package exportador.controller;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import exportador.modelos.ExportarFileRequest;
import exportador.modelos.Respuesta;
import exportador.modelos.RespuestaExito;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/v1/api/")
public class ExportadorController {

	private static final Logger logger = LogManager.getLogger(ExportadorController.class);
	
	@PostMapping(path = "/exportador/bd", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "Hilo iniciado con éxito", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = RespuestaExito.class)) }),
			@ApiResponse(responseCode = "403", description = "Credenciales incorrectas", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = Respuesta.class)) })
			})
	public void exportarBD(@RequestParam("urlConexion") String urlConexion,
			@RequestParam(value= "username") String username,
			@Parameter(schema = @Schema(type = "string", format = "password")) @RequestParam(value= "password") String password,
			@RequestParam(value= "nombreTabla") String nombreTabla,
			@RequestPart(value= "file") MultipartFile file,
			@RequestParam(value= "delimitador", required = false) String delimitador) {
		InputStream is;
		String extensionFichero;
		try {
			extensionFichero = FilenameUtils.getExtension(file.getOriginalFilename());
			is = file.getInputStream();
		} catch (IOException e) {
			logger.error("Se ha producido un error al cargar el fichero proporcionado");
			return;
		}
		HiloExportacionBaseDatos hilo = new HiloExportacionBaseDatos(urlConexion, username, password, nombreTabla, is , delimitador, extensionFichero);
		hilo.setName("HiloExportacionBD");
		hilo.start();
	}
	
	@PostMapping(path = "/exportador/file")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "Hilo iniciado con éxito", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = RespuestaExito.class)) }),
			@ApiResponse(responseCode = "403", description = "Credenciales incorrectas", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = Respuesta.class)) })
			})
	public void exportarFile(
			@RequestBody ExportarFileRequest request) {
		HiloExportacionFichero hilo = new HiloExportacionFichero(request.getUrlConexion(), request.getUsername(), request.getPassword(), request.getNombreTabla(), request.getExtensionFichero(), request.getDelimitador(), request.getCorreo());
		hilo.setName("HiloExportacionFile");
		hilo.start();
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
