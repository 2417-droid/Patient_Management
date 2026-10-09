import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axios';
import { LogIn } from 'lucide-react';

export default function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    
    try {
      // POST to API Gateway auth service
      const response = await api.post('/auth/login', { email, password });
      if (response.data && response.data.token) {
        localStorage.setItem('token', response.data.token);
        navigate('/patients');
      } else {
        setError('Login failed, no token received');
      }
    } catch (err: any) {
      setError(err.response?.data?.message || 'Invalid credentials');
    }
  };

  return (
    <div className="relative min-h-screen flex items-center justify-center bg-canvas overflow-hidden">
      {/* Stripe-inspired atmospheric gradient mesh */}
      <div className="gradient-mesh" />

      <div className="z-10 w-full max-w-md p-8 bg-canvas rounded-xl shadow-level-2 border border-hairline">
        <div className="mb-8 text-center">
          <div className="inline-flex items-center justify-center w-12 h-12 rounded-full bg-primary-soft mb-4 text-on-primary">
            <LogIn size={24} />
          </div>
          <h1 className="display-md text-ink">Sign in to your account</h1>
          <p className="text-ink-mute mt-2">Manage patient data securely</p>
        </div>

        <form onSubmit={handleLogin} className="space-y-4">
          {error && <div className="text-accent-ruby text-sm text-center">{error}</div>}
          
          <div>
            <label className="block text-sm text-ink-secondary mb-1">Email</label>
            <input 
              type="email" 
              className="text-input" 
              value={email}
              onChange={e => setEmail(e.target.value)}
              required
            />
          </div>
          
          <div>
            <label className="block text-sm text-ink-secondary mb-1">Password</label>
            <input 
              type="password" 
              className="text-input" 
              value={password}
              onChange={e => setPassword(e.target.value)}
              required
            />
          </div>

          <button type="submit" className="btn-primary w-full mt-6">
            Sign In
          </button>
        </form>
      </div>
    </div>
  );
}
