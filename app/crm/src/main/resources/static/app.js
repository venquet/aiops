const state = { customers: [], orders: [] };
const $ = (selector) => document.querySelector(selector);

function escapeHtml(value) {
  return String(value).replace(/[&<>'"]/g, (character) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;' }[character]));
}

function formatDate(value) {
  return new Intl.DateTimeFormat(undefined, { month: 'short', day: 'numeric', year: 'numeric' }).format(new Date(value));
}

function formatAmount(amount, currency) {
  return new Intl.NumberFormat(undefined, { style: 'currency', currency }).format(amount);
}

function notify(message, error = false) {
  const element = $('#notification');
  element.textContent = message;
  element.classList.toggle('error', error);
  element.hidden = false;
  window.clearTimeout(notify.timer);
  notify.timer = window.setTimeout(() => { element.hidden = true; }, 4200);
}

async function api(path, options = {}) {
  const response = await fetch(path, { headers: { 'Content-Type': 'application/json' }, ...options });
  if (response.ok) return response.status === 204 ? null : response.json();
  const problem = await response.json().catch(() => ({}));
  throw new Error(problem.detail || `Request failed (${response.status})`);
}

function renderCustomers() {
  $('#customer-count').textContent = state.customers.length;
  $('#customers-empty').hidden = state.customers.length !== 0;
  $('#customer-list').innerHTML = state.customers.map((customer) => `
    <tr><td><span class="customer-name">${escapeHtml(customer.name)}</span><small>${escapeHtml(customer.email)}</small></td>
    <td>${formatDate(customer.createdAt)}</td><td></td></tr>`).join('');
  $('#order-customer').innerHTML = '<option value="">Choose a customer</option>' + state.customers.map((customer) =>
    `<option value="${customer.id}">${escapeHtml(customer.name)} · ${escapeHtml(customer.email)}</option>`).join('');
}

function renderOrders() {
  const customersById = new Map(state.customers.map((customer) => [customer.id, customer]));
  $('#order-count').textContent = state.orders.length;
  $('#pending-count').textContent = state.orders.filter((order) => order.status === 'PENDING').length;
  $('#orders-empty').hidden = state.orders.length !== 0;
  $('#order-list').innerHTML = state.orders.map((order) => {
    const customer = customersById.get(order.customerId);
    return `<tr><td><span class="customer-name">${escapeHtml(order.orderNumber)}</span><small>${formatDate(order.createdAt)}</small></td>
      <td>${escapeHtml(customer?.name || 'Unknown customer')}</td><td class="amount">${formatAmount(order.totalAmount, order.currency)}</td>
      <td><span class="status">${escapeHtml(order.status)}</span></td></tr>`;
  }).join('');
}

async function refresh() {
  try {
    [state.customers, state.orders] = await Promise.all([api('/api/customers'), api('/api/orders')]);
    renderCustomers(); renderOrders();
  } catch (error) { notify(`Could not load CRM data: ${error.message}`, true); }
}

async function checkHealth() {
  const dot = document.querySelector('.status-dot');
  try {
    const health = await api('/actuator/health');
    $('#service-status').textContent = `Service ${health.status.toLowerCase()}`;
    dot.classList.add('online');
  } catch (_) {
    $('#service-status').textContent = 'Service unavailable';
    dot.classList.add('offline');
  }
}

$('#customer-form').addEventListener('submit', async (event) => {
  event.preventDefault();
  const formElement = event.currentTarget;
  const form = new FormData(formElement);
  try {
    await api('/api/customers', { method: 'POST', body: JSON.stringify({ name: form.get('name'), email: form.get('email') }) });
    formElement.reset(); await refresh(); notify('Customer created.');
  } catch (error) { notify(error.message, true); }
});

$('#order-form').addEventListener('submit', async (event) => {
  event.preventDefault();
  const formElement = event.currentTarget;
  const form = new FormData(formElement);
  try {
    await api('/api/orders', { method: 'POST', body: JSON.stringify({ customerId: form.get('customerId'), orderNumber: form.get('orderNumber'), totalAmount: Number(form.get('totalAmount')), currency: String(form.get('currency')).toUpperCase() }) });
    formElement.reset(); formElement.currency.value = 'USD'; await refresh(); notify('Order created.');
  } catch (error) { notify(error.message, true); }
});

document.querySelectorAll('[data-refresh]').forEach((button) => button.addEventListener('click', refresh));
checkHealth(); refresh();
