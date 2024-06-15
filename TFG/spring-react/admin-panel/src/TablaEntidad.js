import React from 'react';
import './tabla.css';

const Tabla = ({ response, currentPage, itemsPerPage }) => {

	if (!response || response.length === 0) return <div>No hay datos disponibles para los parámetros recibidos</div>;

	const keys = Object.keys(response[0].campos);
	
	const startIndex = (currentPage - 1) * itemsPerPage;
	const endIndex = startIndex + itemsPerPage;
	const datosPaginados = response.slice(startIndex, endIndex);

	return (
		<div className="tablaCentrada">
			<table border="1">
				<thead>
					<tr>
						{keys.map((key) => (
							<th key={key}>{key}</th>
						))}
					</tr>
				</thead>
				<tbody>
					{datosPaginados.map((item, index) => (
						
						<tr key={index}>
							{keys.map((key) => (
								<td key={key}>{item.campos[key]}</td>
							))}
						</tr>
					))}
				</tbody>
			</table>
		</div>
	);
};

export default Tabla;