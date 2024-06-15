import React, { useState } from 'react';
import axios from 'axios';
import Tabla from './TablaEntidad';
import Pagination from './Paginacion';

const ObtenerTabla = () => {
    const [response, setResponse] = useState(null);
    const [urlConexion, setUrlConexion] = useState('');
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [nombreTabla, setNombreTabla] = useState('');
    const [currentPage, setCurrentPage] = useState(1);
	const itemsPerPage = 10;
   
	const handleUrlConexion = (e) => {
		setUrlConexion(e.target.value);
	};
	
	const handleUsername = (e) => {
		setUsername(e.target.value);
	};
	
	const handlePassword = (e) => {
		setPassword(e.target.value);
	};
	
	const handleNombreTabla = (e) => {
		setNombreTabla(e.target.value);
	};
	
	const handlePageChange = (page) => {
		setCurrentPage(page);
	};
	
    const handleUpload = async (event) => {
		event.preventDefault();
		
		try {
			const res = await axios.get('/v1/api/obtenerDatos', {
				params: {
					urlConexion: urlConexion,
					username: username,
					password: password,
					nombreTabla: nombreTabla
				}
			});
			console.log(res.data);
			setResponse(res.data);
		} catch (error) {
			console.error('Error fetching data:', error);
		}
    };

    return (
        <div style={{textAlign: "left"}}>
            <h1>Obtener datos de base de datos</h1>
            <div>
	          <label>Url conexion:</label>
	          <input 
	            type="text" 
	            value={urlConexion} 
	            onChange={handleUrlConexion} 
	          />
	        </div>
	        <div>
	          <label>Username:</label>
	          <input 
	            type="text" 
	            value={username} 
	            onChange={handleUsername} 
	          />
	        </div>
	        <div>
	          <label>Password:</label>
	          <input 
	            type="text" 
	            value={password} 
	            onChange={handlePassword} 
	          />
	        </div>
	        <div>
	          <label>Nombre tabla:</label>
	          <input 
	            type="text" 
	            value={nombreTabla} 
	            onChange={handleNombreTabla} 
	          />
	        </div>
            <button onClick={handleUpload}>Upload</button>
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
    );
};

export default ObtenerTabla;