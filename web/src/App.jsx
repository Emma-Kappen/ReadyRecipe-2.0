import React, { useState } from 'react'
import Login from './Login'
import Signup from './Signup'
import Dashboard from './Dashboard'

export default function App() {
  const [route, setRoute] = useState('login')
  const [token, setToken] = useState(localStorage.getItem('token') || null)

  if (token) return <Dashboard token={token} onLogout={() => { localStorage.removeItem('token'); setToken(null); setRoute('login'); }} />

  return (
    <div style={{padding:20,fontFamily:'Arial'}}>
      {route === 'login' && <>
        <h2>Login</h2>
        <Login onLogin={(t)=>{ setToken(t); localStorage.setItem('token', t); }} />
        <p>Don't have an account? <a href="#" onClick={(e)=>{e.preventDefault(); setRoute('signup')}}>Sign up</a></p>
      </>}
      {route === 'signup' && <>
        <h2>Sign Up</h2>
        <Signup onSignup={(t)=>{ setToken(t); localStorage.setItem('token', t); }} />
        <p>Already registered? <a href="#" onClick={(e)=>{e.preventDefault(); setRoute('login')}}>Login</a></p>
      </>}
    </div>
  )
}
