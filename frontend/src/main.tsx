import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.tsx'
import Home from './Home.tsx'
import Space from './Space.tsx'
import { BrowserRouter, Routes, Route } from "react-router";

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<App />}>
          <Route index element={<Home />} /> {/* Renders Home at exactly "/" */}
          <Route path="space" element={<Space />} />
        </Route>
      </Routes>
    </BrowserRouter>
  </StrictMode>
)
