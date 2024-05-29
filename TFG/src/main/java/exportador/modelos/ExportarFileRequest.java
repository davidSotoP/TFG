package exportador.modelos;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

public class ExportarFileRequest {

	@NotBlank
	@NotEmpty
	private String urlConexion;

	@NotBlank
	@NotEmpty
	private String username;
	
	@NotBlank
	@NotEmpty
	private String password;
	
	@NotBlank
	@NotEmpty
	private String nombreTabla;
	
	@NotBlank
	@NotEmpty
	private String extensionFichero;
	
	@NotBlank
	@NotEmpty
	private String correo;
	
	private String delimitador;

	public String getUrlConexion() {
		return urlConexion;
	}

	public void setUrlConexion(String urlConexion) {
		this.urlConexion = urlConexion;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getNombreTabla() {
		return nombreTabla;
	}

	public void setNombreTabla(String nombreTabla) {
		this.nombreTabla = nombreTabla;
	}

	public String getExtensionFichero() {
		return extensionFichero;
	}

	public void setExtensionFichero(String extensionFichero) {
		this.extensionFichero = extensionFichero;
	}

	public String getCorreo() {
		return correo;
	}

	public void setCorreo(String correo) {
		this.correo = correo;
	}

	public String getDelimitador() {
		return delimitador;
	}

	public void setDelimitador(String delimitador) {
		this.delimitador = delimitador;
	}
	
	

	
}
