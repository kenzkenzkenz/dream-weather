import React from 'react';
import logo from './assets/dream-weather-logo-512px.png';

export default function Title() {
    return (
        <div className='header'>
            <img src={logo} alt="Logo" className='logo'></img>
            <h1 className='title'>Dream Weather</h1>
        </div>
    )
}