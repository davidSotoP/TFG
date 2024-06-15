package exportador.modelos;

import java.util.List;

public class RespuestaOperacionesExito {

	public List<DcOperaciones> operaciones; 
	
	public String codigoRespuesta;
	
	public String mensajeRespuesta;
	
	public String getMensajeRespuesta() {
		return mensajeRespuesta;
	}

	public void setMensajeRespuesta(String mensajeRespuesta) {
		this.mensajeRespuesta = mensajeRespuesta;
	}

	public String getCodigoRespuesta() {
		return codigoRespuesta;
	}

	public void setCodigoRespuesta(String codigoRespuesta) {
		this.codigoRespuesta = codigoRespuesta;
	}

	public List<DcOperaciones> getOperaciones() {
		return operaciones;
	}

	public void setOperaciones(List<DcOperaciones> operaciones) {
		this.operaciones = operaciones;
	}
	
}
