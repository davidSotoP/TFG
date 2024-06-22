package exportador.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.multipart.MultipartFile;

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
			logger.info("Se conecta a la base de datos");
			connection = DriverManager.getConnection(urlConexion, username, password);
			logger.info("Conectado");
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

			exportarRapido(connection);
			
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
	
	private void enviarCorreo(String correo, byte[] bytes, int i) throws MessagingException {
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

		ByteArrayResource byteArrayResource = new ByteArrayResource(bytes);
		helper.addAttachment("resultado" + i + ".zip", byteArrayResource);

	    mailSender.send(message);
	}

	public void exportarRapido(Connection connection) throws SQLException, IOException, MessagingException {
		ByteArrayOutputStream byteArrayOutput = new ByteArrayOutputStream(); 
		String copyQuery = "COPY " + nombreTabla + " TO STDOUT WITH CSV HEADER";
		
		CopyManager copyManager = new CopyManager((BaseConnection) connection);
        copyManager.copyOut(copyQuery, byteArrayOutput);
        
        List<byte[]> zipData = dividirArchivoZIP(byteArrayOutput.toByteArray());

        int i = 1;
        for(byte[] correoData: zipData) {
        	enviarCorreo(correo, correoData, i);
        	i++;
        }

	}
	
	private List<byte[]> dividirArchivoZIP(byte[] zip) throws IOException {
		List<byte[]> partes = new ArrayList<>();
        int start = 0;
        int parteNumero = 1;

        while (start < zip.length) {
            ByteArrayOutputStream zipByteArrayOutput = new ByteArrayOutputStream();
            try (ZipOutputStream zipOut = new ZipOutputStream(zipByteArrayOutput)) {
                ZipEntry zipEntry = new ZipEntry("result" + parteNumero + "." + extensionFichero);
                zipOut.putNextEntry(zipEntry);

                int tamanoParte = Math.min(75 * 1024 * 1024, zip.length - start);
                zipOut.write(zip, start, tamanoParte);
                zipOut.closeEntry();
            }

            partes.add(zipByteArrayOutput.toByteArray());
            start += 25 * 1024 * 1024;
            parteNumero++;
        }

        return partes;
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
