document.addEventListener('DOMContentLoaded', () => {
  const form = document.getElementById('login-form');
  const message = document.getElementById('login-message');
  const redirect = new URLSearchParams(window.location.search).get('redirect') || '/reservation.html';

  async function getErrorMessage(res) {
    try {
      const error = await res.json();
      return error.message || '로그인에 실패했습니다.';
    } catch (e) {
      return '로그인에 실패했습니다.';
    }
  }

  form.addEventListener('submit', async (event) => {
    event.preventDefault();
    message.style.display = 'none';

    const response = await fetch('/login', {
      method: 'POST',
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify({
        email: document.getElementById('email').value,
        password: document.getElementById('password').value
      })
    });

    if (response.ok) {
      window.location.href = redirect;
      return;
    }

    message.textContent = await getErrorMessage(response);
    message.style.display = 'block';
  });
});
