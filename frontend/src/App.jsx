import { useState } from 'react'
import './App.css'

function App() {
  const [time, setTime] = useState();
  const [precip, setPrecip] = useState();
  const [temp, setTemp] = useState();

  const handleSubmit = (e) => {
    e.preventDefault();
    const userPrefs = { time, precip, temp };
    console.log(userPrefs);
  }

  return (
    <>
      <h1>Dream Weather</h1>
      <p>
        Tell us what vibe you want, and we'll send you to the perfect location.
      </p>

<form onSubmit={handleSubmit}>
      <strong>Time of Day</strong>
      <div>

        <label style={{ marginRight: "20px" }}>
          <input 
            type="radio" 
            id="day" 
            name="time" 
            value="day" 
            onChange={(e) => setTime(e.target.value)} />
          <label for="day">Day</label>
        </label>

        <label style={{ marginRight: "20px" }}>
          <input 
            type="radio" 
            id="night" 
            name="time" 
            value="night" 
            onChange={(e) => setTime(e.target.value)} />
          <label for="night">Night</label>
        </label>

          <label style={{ marginRight: "20px" }}>
          <input 
            type="radio" 
            id="dawnDusk" 
            name="time" 
            value="dawnDusk" 
            onChange={(e) => setTime(e.target.value)} />
          <label for="dawnDusk">Dawn/Dusk</label>
        </label>
      </div>

      <br/>

      <strong>Precipitation</strong>
      <div>
        <label style={{ marginRight: "20px" }}>
          <input 
            type="radio" 
            id="none" 
            name="precip" 
            value="none"
            onChange={(e) => setPrecip(e.target.value)} />
          <label for="none">None</label>
        </label>

        <label style={{ marginRight: "20px" }}>
          <input 
            type="radio" 
            id="rain" 
            name="precip" 
            value="rain"
            onChange={(e) => setPrecip(e.target.value)} />
          <label for="rain">Rain</label>
        </label>

        <label style={{ marginRight: "20px" }}>
          <input 
            type="radio" 
            id="snow" 
            name="precip" 
            value="snow"
            onChange={(e) => setPrecip(e.target.value)} />
          <label for="snow">Snow</label>
        </label>
      </div>

      <br/>

      <strong>Temperature</strong>
      <div>
        <label style={{ marginRight: "20px" }}>
          <input 
            type="radio" 
            id="cold" 
            name="temp" 
            value="cold"
            onChange={(e) => setTemp(e.target.value)} />
          <label for="cold">Cold</label>
        </label>

        <label style={{ marginRight: "20px" }}>
          <input 
            type="radio" 
            id="mild" 
            name="temp" 
            value="mild"
            onChange={(e) => setTemp(e.target.value)} />
          <label for="mild">Mild</label>
        </label>

        <label style={{ marginRight: "20px" }}>
          <input 
            type="radio" 
            id="hot" 
            name="temp" 
            value="hot"
            onChange={(e) => setTemp(e.target.value)} />
          <label for="hot">Hot</label>
        </label>
      </div>

      <br/>
      <div>
        <button
          onClick={handleSubmit}
          >Let's go!</button>
      </div>
      </form>

    </>
  )
}

export default App
