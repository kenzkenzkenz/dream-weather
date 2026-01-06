import React, { useState, useEffect } from 'react';

export default function Form({ onSubmit }) {

    let countryObj = {
        'name': 'United States',
        'iso_code': 'US',
    }
    const [country, setCountry] = useState(countryObj);
    const [precip, setPrecip] = useState();
    const [temp, setTemp] = useState();
    const [warning, setWarning] = useState("");
    const [disableSubmit, setDisableSubmit] = useState(false);

    useEffect(() => {
        if (precip && temp) {
            if (precip === 'snow' && temp === 'hot') {
                setWarning("The weather doesn't work like that! Try something else.");
                setDisableSubmit(true);
            } else {
                setWarning("");
                setDisableSubmit(false);
            }
        } else {
            setWarning("");
            setDisableSubmit(false);
        }
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

            <h1>Dream Weather</h1>
            <p>Tell us what weather you want, and we'll send you there.</p>

            <form onSubmit={handleSubmit}>
                <strong>Precipitation</strong>
                <div>
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
                <div>
                    <label style={{ marginRight: "20px" }}>
                        <input
                            type="radio"
                            id="cold"
                            name="temp"
                            value="cold"
                            onChange={(e) => setTemp(e.target.value)} />
                        <label htmlFor="cold">Cold</label>
                    </label>

                    <label style={{ marginRight: "20px" }}>
                        <input
                            type="radio"
                            id="hot"
                            name="temp"
                            value="hot"
                            onChange={(e) => setTemp(e.target.value)} />
                        <label htmlFor="hot">Hot</label>
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