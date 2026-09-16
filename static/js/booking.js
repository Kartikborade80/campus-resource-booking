document.addEventListener('DOMContentLoaded', () => {
  const resourceSelect = document.getElementById('resourceSelect');
  const roomSelect = document.getElementById('roomSelect');
  const dateInput = document.getElementById('bookingDate');
  const slotSelect = document.getElementById('slotSelect');
  const statusBox = document.getElementById('availabilityStatus');
  const submitBtn = document.getElementById('submitBookingBtn');

  function checkAvailability() {
    if (!resourceSelect || !dateInput || !slotSelect || !statusBox) return;

    const resourceId = resourceSelect.value;
    const roomId = roomSelect ? roomSelect.value : '';
    const date = dateInput.value;
    const slotId = slotSelect.value;

    if (!resourceId || !date || !slotId) {
      statusBox.innerHTML = '<span style="color: var(--text-muted);">Please select Resource, Date, and Time Slot to verify availability.</span>';
      return;
    }

    statusBox.innerHTML = '<span style="color: var(--info);">Checking live schedule in MySQL database...</span>';

    const params = new URLSearchParams({
      resource_id: resourceId,
      room_id: roomId,
      date: date,
      slot_id: slotId
    });

    fetch(`/api/check-availability?${params.toString()}`)
      .then(res => res.json())
      .then(data => {
        if (data.available) {
          statusBox.innerHTML = `
            <div style="background:#ecfdf5; border:1px solid #a7f3d0; color:#065f46; padding:10px 14px; border-radius:8px; font-weight:600; display:flex; align-items:center; gap:8px;">
              <span>&#10004;</span> ${data.message}
            </div>
          `;
          if (submitBtn) submitBtn.disabled = false;
        } else {
          statusBox.innerHTML = `
            <div style="background:#fef2f2; border:1px solid #fecaca; color:#991b1b; padding:10px 14px; border-radius:8px; font-weight:600; display:flex; align-items:center; gap:8px;">
              <span>&#10008;</span> ${data.message}
            </div>
          `;
          if (submitBtn) submitBtn.disabled = true;
        }
      })
      .catch(err => {
        console.error('Availability check error:', err);
        statusBox.innerHTML = '<span style="color: var(--text-muted);">Could not verify schedule status.</span>';
      });
  }

  if (resourceSelect) resourceSelect.addEventListener('change', checkAvailability);
  if (roomSelect) roomSelect.addEventListener('change', checkAvailability);
  if (dateInput) dateInput.addEventListener('change', checkAvailability);
  if (slotSelect) slotSelect.addEventListener('change', checkAvailability);
});
