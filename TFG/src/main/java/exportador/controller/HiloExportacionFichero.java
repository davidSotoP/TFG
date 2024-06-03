package exportador.controller;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HiloExportacionFichero extends Thread {

	private static final Logger logger = LogManager.getLogger(HiloExportacionFichero.class);

	private String urlConexion;
	private String username;
	private String password;
	private String nombreTabla;
	private MultipartFile file;
	private String extensionFichero;
	private String delimitador;
	private String correo;

	public Map<Object, Class<?>> objeto;
	public static Map<String, Class<?>> TYPE;

	public HiloExportacionFichero(String urlConexion, String username, String password, String nombreTabla,
			String extensionFichero, String delimitador, String correo) {
		super();
		this.urlConexion = urlConexion;
		this.username = username;
		this.password = password;
		this.nombreTabla = nombreTabla;
		this.extensionFichero = extensionFichero;
		this.delimitador = delimitador;
		this.correo = correo;
	}

	public void run() {
		try {
			logger.info("Empieza el hilo con nombre {}", this.getName());
			hiloExportacionFile();
		} catch (SQLException e) {
			logger.error("Se ha producido un error en el hilo de exportacion. Causa: {}", e.getMessage(), e);
		} catch (IOException e) {
			logger.error("Se ha producido un error al crear el fichero con los datos de base de datos. Causa: {}", e.getMessage(), e);
		}
	}

	private void hiloExportacionFile() throws SQLException, IOException {

		Connection connection = null;
		try {
			connection = DriverManager.getConnection(urlConexion, username, password);

			PreparedStatement select = connection.prepareStatement("Select * from " + nombreTabla.toLowerCase());
			ResultSet result = select.executeQuery();
			if (!result.next()) {
				logger.info("No hay ningún registro que migrar");
				return;
			}

			ResultSetMetaData rsmd = result.getMetaData();

			int numeroCol = rsmd.getColumnCount();
			
			if(StringUtils.equals(extensionFichero, "csv")) {
				exportarCSV(result, rsmd, numeroCol);
			} else if(StringUtils.equals(extensionFichero, "json")) {
				exportarJson(result, numeroCol);
			}
			
			
		} catch (SQLException e) {
			logger.error("Error al obtener la conexión con base de datos. Url de conexión: {}. Causa: {}", urlConexion,
					e.getMessage());
		} catch (MessagingException e) {
			logger.error("Error al enviar el correo con direccion: {}. Causa: {}", correo,
					e.getMessage());
		}
	}
	
	private boolean filaVacia(String fila) {
		if(StringUtils.isBlank(fila)) {
			return true;
		} else {
			return false;
		}
	}
	
	private void enviarCorreo(String correo, File file) throws MessagingException {
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
	    helper.setSubject("Resultado exportación");
	    helper.setText("Mensaje autogenerado. No responda este correo por favor.");

	    FileSystemResource fileEnviar = new FileSystemResource(file);
	    helper.addAttachment("resultado.csv", fileEnviar);

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
	
	public void exportarCSV(ResultSet result, ResultSetMetaData rsmd, int NumOfCol) throws SQLException, IOException, MessagingException {
		List<Map<Object, Class<?>>> lista = new ArrayList<>();
		while (result.next()) {
			logger.info("");
			objeto = new HashMap<>();
			for (int i = 1; i <= NumOfCol; i++) {
				String column = rsmd.getColumnTypeName(i).toUpperCase();
				objeto.put(result.getObject(i), TYPE.get(column));
			}
			lista.add(objeto);
		}
		
		List<String> filas = new ArrayList<>();
		for (Map<Object, Class<?>> mapa : lista) {
			String fila = StringUtils.EMPTY;
			for (Object objeto : mapa.keySet()) {
				Class<?> clase = mapa.get(objeto);
				String claseString = clase.getName();
				switch (claseString) {
					case "java.lang.Integer":
						Integer numero = (Integer) objeto;
						if (filaVacia(fila)) {
							fila = numero.toString();
						} else {

							fila = fila + ", " + numero;
						}
						break;
					case "java.lang.Byte":
						Byte byteObjeto = (Byte) objeto;
						if (filaVacia(fila)) {
							fila = byteObjeto.toString();
						} else {

							fila = fila + ", " + byteObjeto.toString();
						}
						break;
					case "java.lang.Short":
						Short shortObjeto = (Short) objeto;
						if (filaVacia(fila)) {
							fila = shortObjeto.toString();
						} else {

							fila = fila + ", " + shortObjeto.toString();
						}
						break;
					case "java.lang.Long":
						Long longObjeto = (Long) objeto;
						if (filaVacia(fila)) {
							fila = longObjeto.toString();
						} else {
							fila = fila + ", " + longObjeto.toString();

						}
						break;
					case "java.lang.Float":
						Float floatObjeto = (Float) objeto;
						if (filaVacia(fila)) {
							fila = floatObjeto.toString();
						} else {

							fila = fila + ", " + floatObjeto.toString();
						}
						break;
					case "java.lang.Double":
						Double doubleObjeto = (Double) objeto;
						if (filaVacia(fila)) {
							fila = doubleObjeto.toString();
						} else {

							fila = fila + ", " + doubleObjeto.toString();
						}
						break;
					case "java.lang.BigDecimal":
						BigDecimal bigDecimalObjeto = (BigDecimal) objeto;
						if (filaVacia(fila)) {
							fila = bigDecimalObjeto.toString();
						} else {

							fila = fila + ", " + bigDecimalObjeto.toString();
						}
						break;
					case "java.lang.Boolean":
						Boolean booleanObjeto = (Boolean) objeto;
						if (filaVacia(fila)) {
							fila = booleanObjeto.toString();
						} else {

							fila = fila + ", " + booleanObjeto.toString();
						}
						break;
					case "java.lang.String":
						String stringObjeto = (String) objeto;
						if (filaVacia(fila)) {
							fila = stringObjeto.toString();
						} else {

							fila = fila + ", " + stringObjeto;
						}
						break;
					case "java.lang.Date":
						Date dateObjeto = (Date) objeto;
						if (filaVacia(fila)) {
							fila = dateObjeto.toString();
						} else {

							fila = fila + ", " + dateObjeto.toString();
						}
						break;
					case "java.lang.Time":
						Time timeObjeto = (Time) objeto;
						if (filaVacia(fila)) {
							fila = timeObjeto.toString();
						} else {

							fila = fila + ", " + timeObjeto.toString();
						}
						break;
					case "java.lang.Timestamp":
						Timestamp timestampObjeto = (Timestamp) objeto;
						if (filaVacia(fila)) {
							fila = timestampObjeto.toString();
						} else {

							fila = fila + ", " + timestampObjeto.toString();
						}
						break;
					default:
						logger.error("El tipo {} no está completado en la aplicación", claseString);
						return;
					}
			}
			filas.add(fila);
		}

		FileUtils.writeLines(new File("C:\\Users\\David\\Documents\\TFG\\result.csv"), "UTF-8", filas);
		File file = new File("C:\\\\Users\\\\David\\\\Documents\\\\TFG\\\\result.csv");
		enviarCorreo(correo, file);

	}
	
	public void exportarJson(ResultSet result, int NumOfCol) throws SQLException, StreamWriteException, DatabindException, IOException, MessagingException {
		List<Map<String, Object>> lista = new ArrayList<>();
		
		while (result.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= NumOfCol; i++) {
                String columnName = result.getMetaData().getColumnName(i);
                Object value = result.getObject(i);
                row.put(columnName, value);
            }
            lista.add(row);
        }
		
		ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(new File("C:\\Users\\David\\Documents\\TFG\\result.csv"), lista);
        File file = new File("C:\\\\Users\\\\David\\\\Documents\\\\TFG\\\\result.csv");
        enviarCorreo(correo, file);
	}

	static {
		TYPE = new HashMap<String, Class<?>>();

		TYPE.put("INTEGER", Integer.class);
		TYPE.put("TINYINT", Byte.class);
		TYPE.put("SMALLINT", Short.class);
		TYPE.put("BIGINT", Long.class);
		TYPE.put("REAL", Float.class);
		TYPE.put("FLOAT", Double.class);
		TYPE.put("DOUBLE", Double.class);
		TYPE.put("DECIMAL", BigDecimal.class);
		TYPE.put("NUMERIC", BigDecimal.class);
		TYPE.put("BOOLEAN", Boolean.class);
		TYPE.put("CHAR", String.class);
		TYPE.put("VARCHAR", String.class);
		TYPE.put("LONGVARCHAR", String.class);
		TYPE.put("DATE", Date.class);
		TYPE.put("TIME", Time.class);
		TYPE.put("TIMESTAMP", Timestamp.class);
		// ...
	}

}
