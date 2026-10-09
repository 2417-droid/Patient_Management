import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import api from '../api/axios';
import { Search, Plus, Edit2, Trash2 } from 'lucide-react';

interface Patient {
  id: string;
  patientId?: string;
  name: string;
  email: string;
  address: string;
  dateOfBirth?: string;
  birthDate?: string;
  registeredDate?: string;
}

interface PageResponse {
  content: Patient[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export default function Patients() {
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(0);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingPatient, setEditingPatient] = useState<Patient | null>(null);
  const queryClient = useQueryClient();

  // Form state
  const [formData, setFormData] = useState({
    name: '', email: '', address: '', birthDate: ''
  });

  const { data, isLoading } = useQuery({
    queryKey: ['patients', search, page],
    queryFn: async () => {
      const res = await api.get(`/api/patients?search=${search}&page=${page}&size=10`);
      return res.data as PageResponse;
    }
  });

  const saveMutation = useMutation({
    mutationFn: async (payload: any) => {
      const patientId = editingPatient ? (editingPatient.id || editingPatient.patientId) : null;
      if (patientId) {
        return api.put(`/api/patients/${patientId}`, payload);
      }
      const createPayload = {
        ...payload,
        registeredDate: payload.registeredDate || new Date().toISOString().split('T')[0]
      };
      return api.post('/api/patients', createPayload);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['patients'] });
      closeModal();
    }
  });

  const deleteMutation = useMutation({
    mutationFn: async (id: string) => api.delete(`/api/patients/${id}`),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['patients'] })
  });

  const updateBillingStatus = useMutation({
    mutationFn: async ({ id, status }: { id: string, status: string }) => {
      return api.patch(`/api/billing/accounts/${id}/status?status=${status}`);
    },
    onSuccess: () => {
      alert("Billing status updated successfully!");
    }
  });

  const openModal = (p?: Patient) => {
    if (p) {
      setEditingPatient(p);
      setFormData({
        name: p.name,
        email: p.email,
        address: p.address,
        birthDate: p.dateOfBirth || p.birthDate || ''
      });
    } else {
      setEditingPatient(null);
      setFormData({ name: '', email: '', address: '', birthDate: '' });
    }
    setIsModalOpen(true);
  };

  const closeModal = () => {
    setIsModalOpen(false);
    setEditingPatient(null);
  };

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault();
    saveMutation.mutate(formData);
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="heading-lg text-ink">Patients</h1>
          <p className="text-ink-mute mt-1">Manage patient records and billing</p>
        </div>
        <button onClick={() => openModal()} className="btn-primary flex items-center gap-2">
          <Plus size={16} /> New Patient
        </button>
      </div>

      <div className="card-light p-0 overflow-hidden">
        <div className="p-4 border-b border-hairline flex gap-4">
          <div className="relative w-72">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-ink-mute" size={16} />
            <input 
              type="text"
              placeholder="Search patients..."
              className="text-input pl-9"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-canvas-soft border-b border-hairline text-ink-mute text-[13px] font-medium tracking-wide">
                <th className="py-3 px-4">Name</th>
                <th className="py-3 px-4">Email</th>
                <th className="py-3 px-4">Birth Date</th>
                <th className="py-3 px-4">Billing Status</th>
                <th className="py-3 px-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="text-sm">
              {isLoading ? (
                <tr><td colSpan={5} className="p-4 text-center text-ink-mute">Loading...</td></tr>
              ) : data?.content.map((p) => {
                const pId = p.id || p.patientId || '';
                return (
                <tr key={pId} className="border-b border-hairline hover:bg-canvas-soft/50 transition-colors">
                  <td className="py-3 px-4 font-medium text-ink">{p.name}</td>
                  <td className="py-3 px-4 text-ink-secondary">{p.email}</td>
                  <td className="py-3 px-4 text-ink-secondary tabular-nums">{p.dateOfBirth || p.birthDate || 'N/A'}</td>
                  <td className="py-3 px-4">
                    <select 
                      className="bg-primary-bg-subdued-hover text-primary-deep text-[11px] uppercase tracking-wider font-medium px-3 py-1 rounded-full border-0 outline-none cursor-pointer"
                      onChange={(e) => updateBillingStatus.mutate({ id: pId, status: e.target.value })}
                      defaultValue="PENDING"
                    >
                      <option value="PENDING">Pending</option>
                      <option value="PAID">Paid</option>
                    </select>
                  </td>
                  <td className="py-3 px-4 text-right space-x-2">
                    <button onClick={() => openModal(p)} className="text-ink-mute hover:text-primary transition-colors">
                      <Edit2 size={16} />
                    </button>
                    <button onClick={() => {
                      if(window.confirm('Delete this patient?')) deleteMutation.mutate(pId);
                    }} className="text-ink-mute hover:text-accent-ruby transition-colors">
                      <Trash2 size={16} />
                    </button>
                  </td>
                </tr>
                );
              })}
            </tbody>
          </table>
        </div>

        <div className="p-4 border-t border-hairline flex justify-between items-center text-sm text-ink-mute">
          <span>Showing page {data ? data.number + 1 : 0} of {data?.totalPages || 0}</span>
          <div className="space-x-2">
            <button 
              disabled={page === 0} 
              onClick={() => setPage(p => p - 1)}
              className="px-3 py-1 border border-hairline rounded hover:bg-canvas-soft disabled:opacity-50"
            >
              Previous
            </button>
            <button 
              disabled={!data || page >= data.totalPages - 1} 
              onClick={() => setPage(p => p + 1)}
              className="px-3 py-1 border border-hairline rounded hover:bg-canvas-soft disabled:opacity-50"
            >
              Next
            </button>
          </div>
        </div>
      </div>

      {isModalOpen && (
        <div className="fixed inset-0 bg-ink/50 backdrop-blur-sm flex items-center justify-center z-50">
          <div className="bg-canvas rounded-xl shadow-level-2 w-full max-w-md overflow-hidden">
            <div className="p-6 border-b border-hairline">
              <h2 className="heading-md">{editingPatient ? 'Edit Patient' : 'New Patient'}</h2>
            </div>
            <form onSubmit={handleSave} className="p-6 space-y-4">
              <div>
                <label className="block text-sm text-ink-secondary mb-1">Name</label>
                <input required type="text" className="text-input" value={formData.name} onChange={e => setFormData({...formData, name: e.target.value})} />
              </div>
              <div>
                <label className="block text-sm text-ink-secondary mb-1">Email</label>
                <input required type="email" className="text-input" value={formData.email} onChange={e => setFormData({...formData, email: e.target.value})} />
              </div>
              <div>
                <label className="block text-sm text-ink-secondary mb-1">Address</label>
                <input required type="text" className="text-input" value={formData.address} onChange={e => setFormData({...formData, address: e.target.value})} />
              </div>
              <div>
                <label className="block text-sm text-ink-secondary mb-1">Birth Date (YYYY-MM-DD)</label>
                <input required type="text" placeholder="1990-01-01" className="text-input tabular-nums" value={formData.birthDate} onChange={e => setFormData({...formData, birthDate: e.target.value})} />
              </div>
              <div className="pt-4 flex justify-end gap-3">
                <button type="button" onClick={closeModal} className="btn-secondary">Cancel</button>
                <button type="submit" className="btn-primary" disabled={saveMutation.isPending}>
                  {saveMutation.isPending ? 'Saving...' : 'Save'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
