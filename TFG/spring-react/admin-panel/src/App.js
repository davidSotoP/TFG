import './App.css';
import React from 'react';
import ExportarDatos from './ExportarDatos';
import ImportarDatos from './ImportarDatos';
import ObtenerTabla from './obtenerTabla';
import Historial from './Historial';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import Navbar from './Navbar';

function App() {
  return (
    <div className="App">
      <header className="App-header">
      	<Navbar />
      	<Router>
	      <div>
	        <div className="content">
		        <Routes>
		          <Route path="/" element={<Home />}></Route>
		          <Route path="/exportar" element={<ExportarDatosConst />} />
		          <Route path="/importar" element={<ImportarDatosConst />} />
		          <Route path="/historial" element={<HistorialConst />} />
		        </Routes>
	        </div>
	      </div>
	    </Router>
      </header>
    </div>
  );
}

const Home = () => (
	<div>
		<ObtenerTabla/>
	</div>
);

const ExportarDatosConst = () => (
  <div>
    <ExportarDatos/>
  </div>
);

const ImportarDatosConst = () => (
  <div>
    <ImportarDatos/>
  </div>
);

const HistorialConst = () => (
  <div>
    <Historial/>
  </div>
);

export default App;
