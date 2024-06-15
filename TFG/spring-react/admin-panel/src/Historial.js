import React, { useState } from 'react';
import axios from 'axios';
import Tabla from './TablaHistorial';
import Pagination from './Paginacion';
import './tabla.css';

const ObtenerTabla = () => {
    const [response, setResponse] = useState(null);
    const [currentPage, setCurrentPage] = useState(1);
	const itemsPerPage = 10;
   
    const handleUpload = async (event) => {
		event.preventDefault();
		
		try {
			const res = await axios.get('/v1/api/obtenerHistorial');
			console.log(res.data);
			setResponse(res.data);
		} catch (error) {
			console.error('Error fetching data:', error);
		}
    };
    
	const handlePageChange = (page) => {
		setCurrentPage(page);
	};

    return (
        <div style={{textAlign: "left"}}>
        	<div className="tablaCentrada"> 
				<h1>Obtener historial de aplicación</h1>
				<button onClick={handleUpload}>Obtener</button>
				{response && (
					<div>
						<h2>Tabla resultados:</h2>
						<Tabla response={response} currentPage={currentPage} itemsPerPage={itemsPerPage} />
						<Pagination
							totalItems={response.length}
							itemsPerPage={itemsPerPage}
							currentPage={currentPage}
							onPageChange={handlePageChange}
						/>
					</div>
				)}
        	</div>
        </div>
    );
};

export default ObtenerTabla;