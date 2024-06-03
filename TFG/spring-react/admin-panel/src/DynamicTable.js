import React, { useState } from 'react';
import axios from 'axios';

const DynamicTable = () => {
    const [entities, setEntities] = useState([]);
    const [file, setFile] = useState(null);

    const handleFileChange = (e) => {
        setFile(e.target.files[0]);
    };

    const handleUpload = () => {
        const formData = new FormData();
        formData.append('file', file);

        axios.post('/api/dynamic-entities/upload', formData)
            .then(response => {
                setEntities(response.data);
            })
            .catch(error => {
                console.error('Error uploading file:', error);
            });
    };

    return (
        <div>
            <h1>Dynamic Data Table</h1>
            <input type="file" onChange={handleFileChange} />
            <button onClick={handleUpload}>Upload</button>
            <table>
                <thead>
                    <tr>
                        {entities.length > 0 && Object.keys(entities[0].fields).map(key => (
                            <th key={key}>{key}</th>
                        ))}
                    </tr>
                </thead>
                <tbody>
                    {entities.map((entity, index) => (
                        <tr key={index}>
                            {Object.values(entity.fields).map((value, idx) => (
                                <td key={idx}>{value}</td>
                            ))}
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
};

export default DynamicTable;