import React from 'react';
import './Navbar.css';
import { Navbar, Nav, Container } from 'react-bootstrap';
import logo from './logo.svg';

const NavigationBar = () => {
  return (
    <Navbar bg="dark" variant="dark" expand="lg">
      <Container>
        <Navbar.Brand href="/"><img src={logo} className="App-logo" alt="logo" /></Navbar.Brand>
        <Navbar.Toggle aria-controls="basic-navbar-nav" />
        <Navbar.Collapse id="basic-navbar-nav">
          <Nav className="me-auto">
            <Nav.Link href="/">Inicio</Nav.Link>
            <Nav.Link href="/exportar">Exportar</Nav.Link>
            <Nav.Link href="/importar">Importar</Nav.Link>
            <Nav.Link href="/historial">Historial</Nav.Link>
          </Nav>
        </Navbar.Collapse>
      </Container>
    </Navbar>
  );
};

export default NavigationBar;