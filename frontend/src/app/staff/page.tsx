import { Header } from '@/components/layout/Header';
import { Card, CardBody } from '@/components/ui/Card';
import { Badge } from '@/components/ui/Badge';
import Link from 'next/link';
import { api } from '@/lib/api';

async function getStaff() {
  try {
    return await api.staff.list();
  } catch {
    return [];
  }
}

export default async function StaffPage() {
  const staff = await getStaff();

  return (
    <div>
      <Header title="Staff" />
      <div className="p-6">
        <Card>
          <CardBody>
            {staff.length === 0 ? (
              <p className="text-gray-500 text-sm">No staff members found.</p>
            ) : (
              <div className="overflow-x-auto">
                <table className="min-w-full text-sm">
                  <thead>
                    <tr className="border-b border-gray-200">
                      <th className="text-left py-3 px-2 font-medium text-gray-600">Name</th>
                      <th className="text-left py-3 px-2 font-medium text-gray-600">Employee ID</th>
                      <th className="text-left py-3 px-2 font-medium text-gray-600">Team</th>
                      <th className="text-left py-3 px-2 font-medium text-gray-600">Role</th>
                      <th className="text-left py-3 px-2 font-medium text-gray-600">Status</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-100">
                    {staff.map((s) => (
                      <tr key={s.id} className="hover:bg-gray-50">
                        <td className="py-3 px-2">
                          <Link href={`/staff/${s.id}`} className="font-medium text-blue-600 hover:underline">
                            {s.name}
                          </Link>
                          <p className="text-xs text-gray-400">{s.email}</p>
                        </td>
                        <td className="py-3 px-2 text-gray-600">{s.employeeId}</td>
                        <td className="py-3 px-2 text-gray-600">{s.team ?? '-'}</td>
                        <td className="py-3 px-2 text-gray-600">{s.role ?? '-'}</td>
                        <td className="py-3 px-2">
                          <Badge variant={s.active ? 'success' : 'default'}>{s.active ? 'Active' : 'Inactive'}</Badge>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </CardBody>
        </Card>
      </div>
    </div>
  );
}
