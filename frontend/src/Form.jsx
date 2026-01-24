import React, { useState, useEffect } from 'react';
import Title from './Title';

export default function Form({ onSubmit }) {

    let countryObj = {
        'name': 'United States',
        'iso_code': 'US',
    }
    const maintenanceMode = false;

    const [country, setCountry] = useState(countryObj);
    const [precip, setPrecip] = useState();
    const [temp, setTemp] = useState();
    const [warning, setWarning] = useState("");
    const [disableSubmit, setDisableSubmit] = useState(true);

    useEffect(() => {
        if (!precip || !temp) {
            setWarning("");
            setDisableSubmit(true);
            return;
        }

        if (precip === 'snow' && temp === 'hot') {
            setWarning("The weather doesn't work like that! Try something else.");
            setDisableSubmit(true);
            return;
        }

        setWarning("");
        setDisableSubmit(false);

    }, [precip, temp]);

    const handleSubmit = (e) => {
        e.preventDefault();
        let req = {
            country: country,
            precipitation: precip,
            temperature: temp,
        };
        onSubmit(req);
    }

    return (
        <>
            {maintenanceMode &&
                <div
                    style={{
                        background: '#d8eaf7',
                        color: 'rgb(51, 41, 41)',
                        padding: '12px',
                        marginBottom: '16px',
                        borderRadius: '6px',
                        fontWeight: 'bold',
                        textAlign: 'center',
                        marginTop: '80px',
                        border: '1px solid rgb(134, 129, 129)',
                    }}
                >
                    {
                        <p>Please note: Some Features are temporarily unavailable due to an issue with an external service provider.
                            <br></br>We are working to resolve it as quickly as possible.</p>
                    }
                </div>}

            <div
                style={{
                    background: warning ? '#ffe0e0' : 'transparent',
                    color: warning ? '#a00' : 'transparent',
                    padding: '12px',
                    marginBottom: '16px',
                    borderRadius: '6px',
                    fontWeight: 'bold',
                    textAlign: 'center',
                    border: warning ? '1px solid #f99' : '1px solid transparent',
                    visibility: warning ? 'visible' : 'hidden',
                }}
            >
                {warning || 'placeholder'}
            </div>

            <Title></Title>
            <p>
                Tell us what vibe you want, and we'll send you to the perfect location.
            </p>

            <form onSubmit={handleSubmit} style={{ padding: "20px" }}>
                <strong>Precipitation</strong>
                <div style={{ padding: "10px" }}>
                    <label style={{ marginRight: "20px" }}>
                        <input
                            type="radio"
                            id="none"
                            name="precip"
                            value="none"
                            onChange={(e) => setPrecip(e.target.value)} />
                        <label htmlFor="none">None</label>
                    </label>

                    <label style={{ marginRight: "20px" }}>
                        <input
                            type="radio"
                            id="rain"
                            name="precip"
                            value="rain"
                            onChange={(e) => setPrecip(e.target.value)} />
                        <label htmlFor="rain">Rain</label>
                    </label>

                    <label style={{ marginRight: "20px" }}>
                        <input
                            type="radio"
                            id="snow"
                            name="precip"
                            value="snow"
                            onChange={(e) => setPrecip(e.target.value)} />
                        <label htmlFor="snow">Snow</label>
                    </label>
                </div>

                <br />

                <strong>Temperature</strong>
                <div style={{ padding: "10px" }}>
                    <label style={{ marginRight: "20px" }}>
                        <input
                            type="radio"
                            id="cold"
                            name="temp"
                            value="cold"
                            onChange={(e) => setTemp(e.target.value)} />
                        <label htmlFor="cold">Cool/Cold</label>
                    </label>

                    <label style={{ marginRight: "20px" }}>
                        <input
                            type="radio"
                            id="hot"
                            name="temp"
                            value="hot"
                            onChange={(e) => setTemp(e.target.value)} />
                        <label htmlFor="hot">Warm/Hot</label>
                    </label>
                </div>

                <br />
                <div>
                    <button
                        onClick={handleSubmit}
                        disabled={disableSubmit}
                        style={disableSubmit ? { backgroundColor: '#ccc', cursor: 'not-allowed' } : {}}
                    >Let's go!</button>
                </div>
            </form>
        </>
    );
}