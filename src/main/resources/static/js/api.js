const API_BASE = '/api';

async function request(url, options = {}) {
    const res = await fetch(API_BASE + url, {
        headers: { 'Content-Type': 'application/json' },
        credentials: 'same-origin',
        ...options
    });
    const data = await res.json().catch(() => ({}));
    if (!res.ok || data.success === false) {
        throw new Error(data.message || `HTTP ${res.status}`);
    }
    return data;
}

const api = {
    getProducts: () => request('/products'),
    getProduct: (id) => request('/products/' + id),

    getCart: () => request('/cart'),
    addToCart: (productId, quantity) => request('/cart/items', {
        method: 'POST',
        body: JSON.stringify({ productId, quantity })
    }),
    updateCartItem: (itemId, quantity) => request(`/cart/items/${itemId}?quantity=${quantity}`, {
        method: 'PUT'
    }),
    removeCartItem: (itemId) => request('/cart/items/' + itemId, { method: 'DELETE' }),

    createOrder: (payload) => request('/orders', {
        method: 'POST',
        body: JSON.stringify(payload)
    }),
    getOrder: (id) => request('/orders/' + id),

    initiatePayment: (orderId, phoneNumber, provider) => request('/payments', {
        method: 'POST',
        body: JSON.stringify({ orderId, phoneNumber, provider })
    }),
    getPayment: (id) => request('/payments/' + id)
};

async function updateCartCount() {
    try {
        const res = await api.getCart();
        const el = document.getElementById('cart-count');
        if (el) el.textContent = res.data.totalItems || 0;
    } catch(e) {}
}