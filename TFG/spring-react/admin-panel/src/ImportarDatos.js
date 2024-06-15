import React, { useState } from 'react';
import axios from 'axios';

const DynamicTable = () => {
    const [response, setResponse] = useState(null);
    const [urlConexion, setUrlConexion] = useState('');
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [nombreTabla, setNombreTabla] = useState('');
	const [extensionFichero, setExtensionFichero] = useState('');
	const [correo, setCorreo] = useState('');
    const [delimitador, setDelimitador] = useState('');
   
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
	
	const handleExtensionFichero = (e) => {
		setExtensionFichero(e.target.value);
	};
	
	const handleCorreo = (e) => {
		setCorreo(e.target.value);
	};
	
	const handleDelimitador = (e) => {
		setDelimitador(e.target.value);
	};

    const handleUpload = async (event) => {
		event.preventDefault();
		
        const ExportarFileRequest = {
			urlConexion : urlConexion,
			username: username,
			password: password,
			nombreTabla: nombreTabla,
			extensionFichero: extensionFichero,
			correo: correo,
			delimitador: delimitador
		}

		try {
			const res = await axios.post('/v1/api/exportador/file', ExportarFileRequest, {
				headers: {
					'Content-Type': 'application/json'
				}
			});
			setResponse(res.data);
		} catch (error) {
			console.error('Error fetching data:', error);
		}
    };

    return (
        <div style={{textAlign: "left"}}>
            <h1>Dynamic Data Table</h1>
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
	        <div>
	          <label>Extensión fichero:</label>
	          <input 
	            type="text" 
	            value={extensionFichero} 
	            onChange={handleExtensionFichero} 
	          />
	        </div>
	        <div>
	          <label>Correo:</label>
	          <input 
	            type="text" 
	            value={correo} 
	            onChange={handleCorreo} 
	          />
	        </div>
	        <div>
	          <label>Delimitador:</label>
	          <input 
	            type="text" 
	            value={delimitador} 
	            onChange={handleDelimitador} 
	          />
	        </div>
            <button onClick={handleUpload}>Upload</button>
			{response && (
				<div>
					<h2>Response Data:</h2>
					<pre>{JSON.stringify(response, null, 2)}</pre>
				</div>
			)}
        </div>
    );
};

export default DynamicTable;