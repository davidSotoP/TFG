package exportador.controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.ByteArrayResource;
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

	public LinkedHashMap<Object, Class<?>> objeto;
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
				insertNuevaEntrada.setObject(4, "Importación");
				insertNuevaEntrada.execute();
				
			} else {
				PreparedStatement insertNuevaEntrada = connection.prepareStatement(
						"INSERT INTO DC_OPERACIONES(x_oper, nombre_tabla, fecha_inicio, tipo) VALUES (?,?,?,?)");
				insertNuevaEntrada.setObject(1, new BigDecimal("1"));
				insertNuevaEntrada.setObject(2, nombreTabla);
				insertNuevaEntrada.setObject(3, new java.sql.Date(new Date().getTime()));
				insertNuevaEntrada.setObject(4, "Importación");
				insertNuevaEntrada.execute();
			}

			PreparedStatement select = connection.prepareStatement("Select * from " + nombreTabla.toLowerCase());
			ResultSet result = select.executeQuery();
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
	
	private void enviarCorreo(String correo, File file, String extension, ByteArrayResource byteArrayResource) throws MessagingException {
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
	    helper.setText("Mensaje autogenerado. No responda este correo por favor.");

	    if(byteArrayResource != null) {
	    	
	    }
	    helper.addAttachment("resultado." + extension, byteArrayResource);

	    mailSender.send(message);
	}

	public void exportarCSV(ResultSet result, ResultSetMetaData rsmd, int NumOfCol) throws SQLException, IOException, MessagingException {
		List<LinkedHashMap <Object, Class<?>>> lista = new ArrayList<>();
		while (result.next()) {
			logger.info("");
			objeto = new LinkedHashMap <>();
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
					case "java.math.BigDecimal":
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

		FileUtils.writeLines(new File("result.csv"), "UTF-8", filas);
		File file = new File("result.csv");
		ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        ZipOutputStream zipOut = new ZipOutputStream(byteArray);
        ZipEntry zipEntry = new ZipEntry(file.getName());
        zipOut.putNextEntry(zipEntry);
        zipOut.closeEntry();
        zipOut.close();
		enviarCorreo(correo, file, extensionFichero, new ByteArrayResource(byteArray.toByteArray()));

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
        mapper.writeValue(new File("result.json"), lista);
        File file = new File("result.json");
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        ZipOutputStream zipOut = new ZipOutputStream(byteArray);
        ZipEntry zipEntry = new ZipEntry(file.getName());
        zipOut.putNextEntry(zipEntry);
        zipOut.closeEntry();
        zipOut.close();
        
        enviarCorreo(correo, file, extensionFichero, new ByteArrayResource(byteArray.toByteArray()));
		
	}

	static {
		TYPE = new HashMap<String, Class<?>>();

		TYPE.put("TEXT", String.class);
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
