<p align="center">
  <img src="assets/dream-weather-logo-512px.png" width="160" alt="Dream Weather logo"/>
</p>

<h1 align="center">Dream Weather</h1>

<p align="center">
  Weather & Livestream Matching Web Application
</p>


# Dream Weather

A React + Spring Boot application that gives users a location match based on their preferred weather conditions. Users get a matched location with title, description, temperature, current conditions, and an embedded livestream.

---

##  Live Demo

This project is hosted at: [https://dream-weather-frontend.onrender.com/](https://dream-weather-frontend.onrender.com/)

---

##  Technologies Used

- **Frontend**: React `^19.2.0`, Vite, JavaScript  
- **Backend**: Java Spring Boot (Java v17 Temurin)  
- **APIs**:  
  - [OpenWebcamDB](https://openwebcamdb.com) — for locations and livestreams  
  - [NOAA / NWS API](https://api.weather.gov) — for current weather data  
- **Node & Package Manager**: Node v20.19.6, npm v10.8.2  

---

##  Features

- Select user preferences for temperature and precipitation.  
- Iterates through multiple webcam locations until a match is found.  
- Fetches gridData from NOAA API to get precise current weather.  
- Returns matched location details: title, description, temperature, forecast, and livestream URL.  
- Handles YouTube and iframe livestreams gracefully.  
- Loader with progress messages while API calls are being made.  
- Fallbacks for unavailable streams or no matches found.

---

##  How It Works

1. **Backend Flow**  
   - Fetches a list of webcam locations from OpenWebcamDB.  
   - Iterates through the list using coordinates to query the NOAA weather API.  
   - Compares the current temperature and forecast with user preferences.  
   - Stops when a matching location is found.  
   - Fetches the webcam livestream URL for that location.  
   - Returns a final object to the frontend with all relevant details.

2. **Frontend Flow**  
   - User submits their weather preferences via a simple form.  
   - Calls the backend and shows a loading state with animated messages.  
   - Displays the matched location with details and embedded livestream.  
   - Provides “Try Again” buttons for errors, no matches, or fetching a new location.  

---

##  Setup Instructions

### Backend

1. Clone the repo and navigate to `/backend`.
2. Ensure you have Java v17 installed (Temurin).  
3. Run the Spring Boot app:  
   `mvn spring-boot:run`
4. The backend runs on `http://localhost:8080` by default.

### Frontend

1. Navigate to `/frontend`.
2. Install dependencies:
   `npm install`
3. Start the development server:
   `npm run dev`
4. React will open `http://localhost:{port}` in your browser.

>  Notes from setup:
>
> * Initially tried `create-react-app`, but Vite was needed for Node v20+.
> * Used `npm create vite@latest` with React (JavaScript + SWC variant).
> * Updated Node.js to v20.19.6 and npm to v10.8.2 to satisfy Vite requirements.

---

##  Lessons Learned

* Always **check API capabilities and limitations** before coding — never make assumptions.
* Plan **UI/UX design** before building to reduce rewrites.
* Learned **server-side caching** concepts, even though it wasn’t implemented in the final version.
* Handling **unavailable livestreams, YouTube restrictions, and no-match scenarios** gracefully is crucial for a smooth UX.
* Some APIs (like OpenWebcamDB) require **API keys** and often have **rate limits** (e.g., 50 calls per day).

---

## Credits

* Webcam data provided by [OpenWebcamDB](https://openwebcamdb.com)
* Weather data provided by [NOAA / NWS](https://weather.gov)
