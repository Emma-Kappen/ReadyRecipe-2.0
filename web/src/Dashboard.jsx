import React from 'react'

export default function Dashboard({ token, onLogout }){
  return (
    <div>
      <h2>Dashboard</h2>
      <p>Logged in. Token length: {token?.length}</p>
      <button onClick={onLogout}>Logout</button>
    </div>
  )
}
