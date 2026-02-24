import React, { useState } from 'react'
import axios from 'axios'

export default function Signup({ onSignup }){
  const [email,setEmail]=useState('')
  const [password,setPassword]=useState('')
  const [error,setError]=useState(null)

  const submit = async (e) => {
    e.preventDefault()
    try{
      const res = await axios.post('http://localhost:8080/api/signup', { email, password })
      onSignup(res.data.token)
    }catch(err){
      setError(err.response?.data?.error || 'Signup failed')
    }
  }

  return (
    <form onSubmit={submit} style={{maxWidth:400}}>
      <div><input placeholder="Email" value={email} onChange={e=>setEmail(e.target.value)} style={{width:'100%',padding:8,marginBottom:8}}/></div>
      <div><input placeholder="Password" type="password" value={password} onChange={e=>setPassword(e.target.value)} style={{width:'100%',padding:8,marginBottom:8}}/></div>
      <div><button type="submit">Sign Up</button></div>
      {error && <div style={{color:'red',marginTop:8}}>{error}</div>}
    </form>
  )
}
