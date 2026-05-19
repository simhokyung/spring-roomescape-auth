document.addEventListener('DOMContentLoaded', async () => {
  const resThemeSelect = document.getElementById('res-theme');
  const resDateInput = document.getElementById('res-date');
  const resTimeSelect = document.getElementById('res-time');
  const resForm = document.getElementById('reservation-form');
  const myReservationList = document.getElementById('my-reservation-list');
  const myReservationMessage = document.getElementById('my-reservation-message');
  const loginMemberName = document.getElementById('login-member-name');
  const logoutButton = document.getElementById('logout-button');
  const reloadButton = document.getElementById('reload-my-reservations');

  let myReservations = [];

  const today = new Date().toISOString().split('T')[0];
  resDateInput.min = today;

  document.querySelectorAll('.page-tab').forEach(tab => {
    tab.addEventListener('click', () => {
      const targetId = tab.dataset.tabTarget;
      document.querySelectorAll('.page-tab').forEach(item => item.classList.remove('active'));
      document.querySelectorAll('.tab-panel').forEach(panel => panel.classList.remove('active'));
      tab.classList.add('active');
      document.getElementById(targetId).classList.add('active');

      if (targetId === 'manage-reservation') {
        loadMyReservations();
      }
    });
  });

  function redirectToLogin() {
    window.location.href = `/login.html?redirect=${encodeURIComponent(window.location.pathname + window.location.search)}`;
  }

  async function getErrorMessage(res) {
    const fallbackMessage = '요청을 처리하지 못했습니다. 잠시 후 다시 시도하세요.';

    if (res.status === 401) {
      redirectToLogin();
      return '로그인이 필요합니다.';
    }

    try {
      const error = await res.clone().json();
      return error.action || error.message || fallbackMessage;
    } catch (e) {
      try {
        const text = await res.text();
        return text || fallbackMessage;
      } catch (e) {
        return fallbackMessage;
      }
    }
  }

  function showMyReservationMessage(message, type = 'info') {
    myReservationMessage.textContent = message;
    myReservationMessage.className = `status-message ${type}`;
    myReservationMessage.style.display = 'block';
  }

  function hideMyReservationMessage() {
    myReservationMessage.textContent = '';
    myReservationMessage.style.display = 'none';
  }

  async function loadCurrentMember() {
    const res = await fetch('/members/me');
    if (res.status === 401) {
      redirectToLogin();
      return null;
    }
    if (!res.ok) {
      throw new Error(await getErrorMessage(res));
    }

    return res.json();
  }

  async function loadThemes() {
    const urlParams = new URLSearchParams(window.location.search);
    const initialThemeId = urlParams.get('themeId');
    const res = await fetch('/themes');

    if (!res.ok) {
      alert(await getErrorMessage(res));
      return;
    }

    const themes = await res.json();
    resThemeSelect.innerHTML = '<option value="">테마를 선택하세요</option>';
    themes.forEach(theme => {
      const isSelected = initialThemeId && theme.id.toString() === initialThemeId ? 'selected' : '';
      resThemeSelect.innerHTML += `<option value="${theme.id}" ${isSelected}>${theme.name}</option>`;
    });
  }

  async function checkAvailableTimes() {
    const themeId = resThemeSelect.value;
    const date = resDateInput.value;
    if (!themeId || !date) {
      resTimeSelect.innerHTML = '<option value="">날짜와 테마를 먼저 선택하세요</option>';
      resTimeSelect.disabled = true;
      return;
    }

    const res = await fetch(`/themes/${themeId}/reservation-times?date=${date}`);
    if (!res.ok) {
      alert(await getErrorMessage(res));
      return;
    }

    const times = await res.json();
    const availableTimes = times.filter(time => time.available);

    if (availableTimes.length === 0) {
      resTimeSelect.innerHTML = '<option value="">예약 가능한 시간이 없습니다</option>';
      resTimeSelect.disabled = true;
      return;
    }

    resTimeSelect.innerHTML = '<option value="">시간을 선택하세요</option>';
    availableTimes.forEach(time => {
      resTimeSelect.innerHTML += `<option value="${time.id}">${time.startAt}</option>`;
    });
    resTimeSelect.disabled = false;
  }

  async function createReservation(event) {
    event.preventDefault();

    const response = await fetch('/reservations', {
      method: 'POST',
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify({
        date: resDateInput.value,
        themeId: resThemeSelect.value,
        timeId: resTimeSelect.value
      })
    });

    if (!response.ok) {
      alert(await getErrorMessage(response));
      return;
    }

    alert('예약이 완료되었습니다.');
    await checkAvailableTimes();
    await loadMyReservations();
  }

  async function loadMyReservations() {
    const res = await fetch('/reservations/mine');
    if (!res.ok) {
      showMyReservationMessage(await getErrorMessage(res), 'error');
      return;
    }

    const data = await res.json();
    myReservations = data.reservations || [];
    renderMyReservations();
  }

  function renderMyReservations() {
    hideMyReservationMessage();
    myReservationList.innerHTML = '';

    if (myReservations.length === 0) {
      myReservationList.innerHTML = '<tr><td colspan="4" class="table-empty">예약이 없습니다.</td></tr>';
      showMyReservationMessage('조회된 예약이 없습니다.', 'info');
      return;
    }

    myReservations.forEach(reservation => {
      const row = document.createElement('tr');
      row.innerHTML = `
        <td>
          <strong>${reservation.theme.name}</strong>
          <div class="table-muted">예약번호 ${reservation.id}</div>
        </td>
        <td>
          <input type="date" class="form-control table-control" value="${reservation.date}" min="${today}" data-update-date="${reservation.id}">
        </td>
        <td>
          <select class="form-control table-control" data-update-time="${reservation.id}">
            <option value="${reservation.time.id}">${reservation.time.startAt}</option>
          </select>
        </td>
        <td>
          <div class="row-actions">
            <button type="button" class="btn-secondary btn-small" data-action="load-times" data-id="${reservation.id}">시간 조회</button>
            <button type="button" class="btn-primary btn-small" data-action="update" data-id="${reservation.id}">변경</button>
            <button type="button" class="btn-danger btn-small" data-action="cancel" data-id="${reservation.id}">취소</button>
          </div>
        </td>
      `;
      myReservationList.appendChild(row);
    });
  }

  async function loadAvailableTimesForReservation(id) {
    const reservation = myReservations.find(item => item.id === id);
    const dateInput = document.querySelector(`[data-update-date="${id}"]`);
    const timeSelect = document.querySelector(`[data-update-time="${id}"]`);
    const date = dateInput.value;

    if (!reservation || !date) {
      showMyReservationMessage('변경할 날짜를 선택하세요.', 'error');
      return;
    }

    const res = await fetch(`/themes/${reservation.theme.id}/reservation-times?date=${date}`);
    if (!res.ok) {
      showMyReservationMessage(await getErrorMessage(res), 'error');
      return;
    }

    const times = await res.json();
    const availableTimes = times.filter(time => time.available || time.id === reservation.time.id);
    timeSelect.innerHTML = '';

    if (availableTimes.length === 0) {
      timeSelect.innerHTML = '<option value="">예약 가능한 시간이 없습니다</option>';
      showMyReservationMessage('선택한 날짜에 예약 가능한 시간이 없습니다.', 'info');
      return;
    }

    availableTimes.forEach(time => {
      const selected = time.id === reservation.time.id ? 'selected' : '';
      timeSelect.innerHTML += `<option value="${time.id}" ${selected}>${time.startAt}</option>`;
    });
    hideMyReservationMessage();
  }

  async function updateMyReservation(id) {
    const date = document.querySelector(`[data-update-date="${id}"]`).value;
    const timeId = document.querySelector(`[data-update-time="${id}"]`).value;

    if (!date || !timeId) {
      showMyReservationMessage('변경할 날짜와 시간을 선택하세요.', 'error');
      return;
    }

    const res = await fetch(`/reservations/${id}`, {
      method: 'PATCH',
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify({date, timeId})
    });

    if (!res.ok) {
      showMyReservationMessage(await getErrorMessage(res), 'error');
      return;
    }

    await loadMyReservations();
    showMyReservationMessage('예약이 변경되었습니다.', 'success');
  }

  async function cancelMyReservation(id) {
    if (!confirm('예약을 취소하시겠습니까?')) {
      return;
    }

    const res = await fetch(`/reservations/${id}`, {method: 'DELETE'});
    if (!res.ok) {
      showMyReservationMessage(await getErrorMessage(res), 'error');
      return;
    }

    await loadMyReservations();
    showMyReservationMessage('예약이 취소되었습니다.', 'success');
    await checkAvailableTimes();
  }

  async function logout() {
    await fetch('/logout', {method: 'POST'});
    window.location.href = '/login.html';
  }

  resThemeSelect.addEventListener('change', checkAvailableTimes);
  resDateInput.addEventListener('change', checkAvailableTimes);
  resForm.addEventListener('submit', createReservation);
  reloadButton.addEventListener('click', loadMyReservations);
  logoutButton.addEventListener('click', logout);

  myReservationList.addEventListener('change', (event) => {
    const id = Number(event.target.dataset.updateDate);
    if (id) {
      loadAvailableTimesForReservation(id);
    }
  });

  myReservationList.addEventListener('click', (event) => {
    const button = event.target.closest('button[data-action]');
    if (!button) {
      return;
    }

    const id = Number(button.dataset.id);
    if (button.dataset.action === 'load-times') {
      loadAvailableTimesForReservation(id);
    }
    if (button.dataset.action === 'update') {
      updateMyReservation(id);
    }
    if (button.dataset.action === 'cancel') {
      cancelMyReservation(id);
    }
  });

  const member = await loadCurrentMember();
  if (!member) {
    return;
  }

  loginMemberName.textContent = `${member.name}님 로그인 중`;
  await loadThemes();
  await loadMyReservations();
});
