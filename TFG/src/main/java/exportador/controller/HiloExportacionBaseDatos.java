package exportador.controller;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HiloExportacionBaseDatos extends Thread {

	private static final Logger logger = LogManager.getLogger(HiloExportacionBaseDatos.class);

	private String urlConexion;
	private String username;
	private String password;
	private String nombreTabla;
	private InputStream file;
	private String extensionFichero;
	private String delimitador;
	private String correo;

	public Map<Object, Class<?>> objeto;
	public static Map<String, Class<?>> TYPE;

	public HiloExportacionBaseDatos(String urlConexion, String username, String password, String nombreTabla, InputStream file,
			String correo, String delimitador, String extensionFichero) {
		super();
		this.urlConexion = urlConexion;
		this.username = username;
		this.password = password;
		this.nombreTabla = nombreTabla;
		this.file = file;
		this.correo = correo;
		this.delimitador = delimitador;
		this.extensionFichero = extensionFichero;
	}

	public void run() {
		try {
			logger.info("Empieza el hilo con nombre {}", this.getName());
			hiloExportacionBD();
		} catch (SQLException | IOException e) {
			logger.error("Se ha producido un error en el hilo de exportacion. Causa: {}", e.getMessage(), e);
		}
	}

	private void hiloExportacionBD() throws SQLException, IOException {

		Connection connection = null;
		try {
			connection = DriverManager.getConnection(urlConexion, username, password);
			Statement stm = connection.createStatement();
			
			InputStreamReader is = new InputStreamReader(file, "UTF-8");
			
			PreparedStatement obtenerUltimoValor = connection.prepareStatement(
					"SELECT DISTINCT ON (x_oper) * FROM DC_OPERACIONES ORDER BY x_oper DESC;");
			ResultSet checkResult = obtenerUltimoValor.executeQuery();
			if(checkResult.next()) {
				PreparedStatement insertNuevaEntrada = connection.prepareStatement(
						"INSERT INTO DC_OPERACIONES(x_oper, nombre_tabla, fecha_inicio, tipo) VALUES (?,?,?,?)");
				int primaryKey = checkResult.getInt(1);
				insertNuevaEntrada.setObject(1, primaryKey + 1);
				insertNuevaEntrada.setObject(2, nombreTabla);
				insertNuevaEntrada.setObject(3, new java.sql.Date(new Date().getTime()));
				insertNuevaEntrada.setObject(4, "Exportación");
				insertNuevaEntrada.execute();
				
			} else {
				PreparedStatement insertNuevaEntrada = connection.prepareStatement(
						"INSERT INTO DC_OPERACIONES(x_oper, nombre_tabla, fecha_inicio, tipo) VALUES (?,?,?,?)");
				insertNuevaEntrada.setObject(1, new BigDecimal("1"));
				insertNuevaEntrada.setObject(2, nombreTabla);
				insertNuevaEntrada.setObject(3, new java.sql.Date(new Date().getTime()));
				insertNuevaEntrada.setObject(4, "Exportación");
				insertNuevaEntrada.execute();
			}
			
			if(StringUtils.equals(extensionFichero, "csv")) {
				exportarCSV(connection, is);
			} else if(StringUtils.equals(extensionFichero, "json")) {
				exportJsonToDatabase(file, connection, nombreTabla);
			}
			
			enviarCorreo(correo);
		} catch (SQLException e) {
			logger.error("Error al obtener la conexión con base de datos. Url de conexión: {}. Causa: {}", urlConexion,
					e.getMessage(), e);
		} catch (MessagingException e) {
			logger.error("Error al enviar el correo. Causa: {}",
					e.getMessage(), e);
		}
	}
	
	private void exportarCSV(Connection connection, InputStreamReader is) throws SQLException {
		try (BufferedReader br = new BufferedReader(is)) {
			String line;
			int clavePrimaria = 0;

			PreparedStatement createTable2 = connection.prepareStatement(
					"CREATE TABLE IF NOT EXISTS " + nombreTabla + "Intermedia(clave varchar(500), completado varchar(1))");
			createTable2.execute();

			List<String> lineas = new ArrayList<>();
			while ((line = br.readLine()) != null) {
				lineas.add(line);
				if (clavePrimaria == 0) {
					clavePrimaria++;
					continue;
				}
				StringBuilder sb = new StringBuilder(line);
				sb.replace(0, 1, "");
				sb.replace(sb.length() - 1, sb.length(), "");
				line = sb.toString();
				String[] values = line.split(";");

				PreparedStatement selectLastResult = connection
						.prepareStatement("Select clave from " + nombreTabla.toLowerCase()
								+ "intermedia where clave = '" + String.valueOf(clavePrimaria) + "'");
				ResultSet checkResult = selectLastResult.executeQuery();
				if (checkResult.next()) {
					logger.info("Ya existe el registro con clave {}", clavePrimaria);
					clavePrimaria++;
					continue;
				}
				PreparedStatement insertIntermedio = connection.prepareStatement(
						"INSERT INTO " + nombreTabla + "Intermedia(clave) VALUES (?)");
				insertIntermedio.setObject(1, clavePrimaria);
				insertIntermedio.execute();
				clavePrimaria++;
			}

			logger.info("Se han migrado {} elementos a la tabla intermedia {}", clavePrimaria,
					nombreTabla + "intermedia");
			
			rellenarTablaPrincipal(connection, lineas);
		} catch (FileNotFoundException e) {
			logger.error("Se ha producido un error al actualizar la tabla {}. Causa: {}", nombreTabla,
					e.getMessage(), e);
		} catch (IOException e) {
			logger.error("Se ha producido un error al actualizar la tabla {}. Causa: {}", nombreTabla,
					e.getMessage(), e);
		}
	}
	
	private void rellenarTablaPrincipal(Connection connection, List<String> lineas) throws SQLException, IOException {

		String columnasCrear = StringUtils.EMPTY;
		String columnasInsert = StringUtils.EMPTY;
		String valores = StringUtils.EMPTY;
		int i = 0;
		int clavePrimaria = 0;
		int ultimoElemento = 0;

		PreparedStatement selectLastResult = connection
				.prepareStatement("Select clave from " + nombreTabla.toLowerCase()
						+ "intermedia where (completado != 'S' or completado is null) order by clave asc");
		ResultSet checkResult = selectLastResult.executeQuery();
		if (!checkResult.next()) {
			logger.info("No hay ningún registro más que migrar");
			return;
		} else {
			ultimoElemento = Integer.valueOf(checkResult.getString(1));
		}
		
		for (String line : lineas) {
			StringBuilder sb = new StringBuilder(line);
			sb.replace(0, 1, "");
			sb.replace(sb.length() - 1, sb.length(), "");
			line = sb.toString();
			String[] values = line.split(";");
			
			if (i != 0) {
				while (clavePrimaria < ultimoElemento) {
					clavePrimaria++;
					continue;
				}
			}
			
			// Se añade el parámetro para clave primaria
			String parametros = "?, ";
			for (int j = 0; j < values.length; j++) {
				if (j == 0) {
					parametros = parametros + "?";
				} else {
					parametros = parametros + ", ?";
				}
			}
			
			int j = 0;
			for (String valor : values) {
				if (i == 0) {
					if (j == values.length - 1) {
						columnasCrear = columnasCrear + valor + " varchar(255)";
						columnasInsert = columnasInsert + valor;
						PreparedStatement createTable = connection.prepareStatement(
								"CREATE TABLE IF NOT EXISTS " + nombreTabla + "(" + columnasCrear + ")");
						createTable.execute();
						
					} else if (j == 0) {
						columnasCrear = "x_tabla numeric primary key," + columnasCrear + valor + " varchar(255), ";
						columnasInsert = "x_tabla, " + columnasInsert + valor + ", ";
					} else {
						columnasCrear = columnasCrear + valor + " varchar(255), ";
						columnasInsert = columnasInsert + valor + ", ";
					}
					
				} else {
					if (j == values.length - 1) {
						valores = valores + valor;
						PreparedStatement insert = connection.prepareStatement("INSERT INTO " + nombreTabla
								+ "(" + columnasInsert + ") VALUES (" + parametros + ")");
						insert.setObject(1, clavePrimaria);
						for (int z = 0; z < values.length; z++) {
							insert.setObject(z + 2, values[z]);
						}
						insert.execute();
						PreparedStatement updateTable = connection.prepareStatement("UPDATE "
								+ nombreTabla.toLowerCase() + "intermedia set completado = 'S' where clave = '"
								+ clavePrimaria + "'");
						updateTable.execute();
					} else if (j == 0) {
						valores = String.valueOf(clavePrimaria);
					} else {
						valores = valores + valor + ", ";
					}
				}
				j++;
			}
			j = 0;
			i++;
			clavePrimaria++;
		}
	}
	
	public static void exportJsonToDatabase(InputStream file, Connection connection, String nombreTabla) {
        try {
        	
			ObjectMapper objectMapper = new ObjectMapper();
			List<Map<String, Object>> mapa = objectMapper.readValue(file, new TypeReference<List<Map<String, Object>>>(){});

			if (CollectionUtils.isEmpty(mapa)) {
				System.out.println("El fichero JSON está vacío.");
				return;
			}
			
			int clavePrimaria = 1;

			PreparedStatement createTable2 = connection.prepareStatement(
					"CREATE TABLE IF NOT EXISTS " + nombreTabla + "Intermedia(clave varchar(500), completado varchar(1))");
			createTable2.execute();
			
			while(clavePrimaria < mapa.size()) {
				PreparedStatement selectLastResult = connection
						.prepareStatement("Select clave from " + nombreTabla.toLowerCase()
						+ "intermedia where clave = '" + String.valueOf(clavePrimaria) + "'");
				ResultSet checkResult = selectLastResult.executeQuery();
				if (checkResult.next()) {
					logger.info("Ya existe el registro con clave {}", clavePrimaria);
					clavePrimaria++;
					continue;
				}
				
				PreparedStatement insertIntermedio = connection.prepareStatement(
						"INSERT INTO " + nombreTabla + "Intermedia(clave) VALUES (?)");
				insertIntermedio.setObject(1, clavePrimaria);
				insertIntermedio.execute();
				clavePrimaria++;
			}
			
			StringBuilder sql = new StringBuilder("INSERT INTO " +nombreTabla.toLowerCase() + "(");
			StringBuilder createTable = new StringBuilder(
					"CREATE TABLE " + nombreTabla + "(");
			for (String key : mapa.get(0).keySet()) {
				sql.append(key).append(",");
				createTable.append(key).append(" TEXT,");
			}
			createTable.setLength(createTable.length() - 1);
			createTable.append(");");
			
			sql.setLength(sql.length() - 1); // Eliminar la última coma
			sql.append(") VALUES (");
			for (int i = 0; i < mapa.get(0).size(); i++) {
				sql.append("?,");
			}
			sql.setLength(sql.length() - 1); // Eliminar la última coma
			sql.append(");");
			
			try (Statement  pstmt = connection.createStatement()) {
				pstmt.execute(createTable.toString());
			}

			try (PreparedStatement pstmt = connection.prepareStatement(sql.toString())) {
				
				PreparedStatement selectTabllPrincipañ = connection
						.prepareStatement("Select count(*) from information_schema.tables where table_name= '"
								+ nombreTabla.toLowerCase() + "' LIMIT " + 1);
				ResultSet checkTablePrincipal = selectTabllPrincipañ.executeQuery();
				checkTablePrincipal.next();
				if (checkTablePrincipal.getInt(1) != 0) {
					logger.error("Ya existe la tabla {}", nombreTabla);
				}
				int ultimoElemento = 0;

				PreparedStatement selectLastResult = connection
						.prepareStatement("Select clave from " + nombreTabla.toLowerCase()
								+ "intermedia where (completado != 'S' or completado is null) order by clave asc");
				ResultSet checkResult = selectLastResult.executeQuery();
				if (!checkResult.next()) {
					logger.info("No hay ningún registro más que migrar");
				} else {
					ultimoElemento = Integer.valueOf(checkResult.getString(1));
				}
				clavePrimaria = 1;
				for (Map<String, Object> row : mapa) {
					int index = 1;
					
					if(clavePrimaria < ultimoElemento) {
						clavePrimaria++;
						continue;
					}
					
					for (Object value : row.values()) {
						pstmt.setObject(index++, value);
					}
					pstmt.addBatch();
				}
				pstmt.executeBatch();
			}

			System.out.println("Datos exportados exitosamente");

        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }
	
	private void enviarCorreo(String correo) throws MessagingException {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
	    mailSender.setHost("mail.us.es");
	    mailSender.setPort(587);
	    
	    mailSender.setUsername("davsotpon@alum.us.es");
	    mailSender.setPassword("CAracteres.1999");
	    
	    Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");
	    
	    MimeMessage message = mailSender.createMimeMessage();
	    MimeMessageHelper helper = new MimeMessageHelper(message, true);

	    helper.setTo(correo);
	    helper.setFrom(mailSender.getUsername());
	    helper.setSubject("Resultado exportación");
	    helper.setText("Se ha completado la exportación con éxito. Puede consultar los datos guardados en la aplicación."
	    		+ " (Mensaje autogenerado. No responda este correo por favor)");

	    mailSender.send(message);
	}

	public static void main(String[] args) throws SQLException {
		// Estos serían los parámetros
		String url = "jdbc:postgresql://localhost:5432/postgres";
		String user = "postgres";
		String password = "postgres";
		String nombreTabla = "PRUEBA";

		Connection connection = null;
		try {
			connection = DriverManager.getConnection(url, user, password);
			Statement stm = connection.createStatement();
			PreparedStatement createTable = connection.prepareStatement("SELECT * FROM " + nombreTabla);
			ResultSet rs = createTable.executeQuery();
			ResultSetMetaData columnas = rs.getMetaData();
			int numColumna = columnas.getColumnCount();
			// TODO Pegar datos al csv
//			for (int i=0; i < numColumna; ++i) {
//			    String name = columnas.getColumnName(i+1);
//			    if (i > 0) {
//			        sb.append(", ");
//			    }
//			    sb.append(name);
//			}
			while (rs.next()) {

			}

		} catch (SQLException e) {
			System.out.println("Error al obtener la conexión con base de datos. Url de conexión: " + url + ". Causa: "
					+ e.getMessage());
			e.printStackTrace();
		}
	}

}
