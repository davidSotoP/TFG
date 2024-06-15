package exportador.modelos;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Data;

@Data
@Entity
@Table(name = "DC_OPERACIONES")
public class DcOperaciones implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1322766295226244798L;

	private Long xOper;
	
	private String nombreTabla;
	
	private Date fechaInicio;
	
	private String tipo;

	@Id
	@Column(name="X_OPER", unique = true, nullable=false, precision = 12, scale=0)
	public Long getxOper() {
		return xOper;
	}

	public void setxOper(Long xOper) {
		this.xOper = xOper;
	}

	@Column(name = "nombre_tabla", nullable = false, length = 200)
	public String getNombreTabla() {
		return nombreTabla;
	}

	public void setNombreTabla(String nombreTabla) {
		this.nombreTabla = nombreTabla;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "fecha_inicio", length = 7)
	public Date getFechaInicio() {
		return fechaInicio;
	}

	public void setFechaInicio(Date fechaInicio) {
		this.fechaInicio = fechaInicio;
	}

	@Column(name = "tipo", nullable = false, length = 20)
	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	
	
}
