package exportador.repositorios;

import java.io.Serializable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import exportador.modelos.DcOperaciones;

@Repository
public interface OperacionesRepository extends JpaRepository<DcOperaciones, Long>, Serializable {

	
}
