const orderId = process.argv[2] || '1145b618-365e-41fb-93fe-fe86d67adbc2';
const username = process.argv[3] || 'superadmin';
const password = process.argv[4] || 'Admin@123';
const baseUrl = 'http://localhost:9191';

async function main() {
  const loginRes = await fetch(`${baseUrl}/api/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password }),
  });
  const loginJson = await loginRes.json();
  if (!loginRes.ok || !loginJson.token) {
    throw new Error(`Login failed: ${loginRes.status} ${JSON.stringify(loginJson)}`);
  }

  const headers = { Authorization: `Bearer ${loginJson.token}` };

  const internalHeaders = { 'X-Internal-Service-Key': 'brickworks-internal-dev-key' };
  const orderRes = await fetch(`http://localhost:8080/api/orders/${orderId}`, { headers: internalHeaders });
  const orderText = await orderRes.text();
  console.log('Direct order fetch:', orderRes.status, orderText.slice(0, 300));

  const generateRes = await fetch(`${baseUrl}/api/finance/invoice/generate/${orderId}`, {
    method: 'POST',
    headers: { ...headers, 'Content-Type': 'application/json' },
    body: '{}',
  });
  const generateText = await generateRes.text();
  console.log('Generate:', generateRes.status, generateText);

  const downloadRes = await fetch(`${baseUrl}/api/finance/invoice/download/${orderId}`, { headers });
  console.log('Download:', downloadRes.status, downloadRes.headers.get('content-type'));
  const buf = Buffer.from(await downloadRes.arrayBuffer());
  if (!downloadRes.ok) {
    console.log('Download body:', buf.toString('utf8').slice(0, 500));
    process.exit(1);
  }
  if (buf.length < 500) {
    console.log('Download too small:', buf.length);
    process.exit(1);
  }
  console.log('Download bytes:', buf.length);
  console.log('Invoice API test passed');
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});