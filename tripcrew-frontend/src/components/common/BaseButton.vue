<template>
  <button
    :class="['base-btn', `base-btn--${variant}`, `base-btn--${size}`, { 'is-full': full }]"
    :type="type"
    :disabled="disabled"
  >
    <slot />
  </button>
</template>

<script setup>
defineProps({
  variant: {
    type: String,
    default: 'primary',
    validator: v => ['primary', 'secondary', 'ghost', 'danger'].includes(v)
  },
  size: {
    type: String,
    default: 'md',
    validator: v => ['sm', 'md', 'lg'].includes(v)
  },
  type: { type: String, default: 'button' },
  full: { type: Boolean, default: false },
  disabled: { type: Boolean, default: false }
})
</script>

<style scoped>
.base-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  border-radius: 10px;
  font-weight: 600;
  transition: all 0.15s;
  white-space: nowrap;
}

.base-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* sizes */
.base-btn--sm { padding: 7px 14px; font-size: 13px; }
.base-btn--md { padding: 10px 18px; font-size: 14px; }
.base-btn--lg { padding: 14px 24px; font-size: 16px; border-radius: 12px; }

.is-full { width: 100%; }

/* variants */
.base-btn--primary {
  background: var(--coral);
  color: white;
}
.base-btn--primary:hover:not(:disabled) {
  background: var(--coral-2);
  transform: translateY(-1px);
  box-shadow: var(--sh-2);
}

.base-btn--secondary {
  background: var(--surface);
  color: var(--ink);
  border: 1px solid var(--line-2);
}
.base-btn--secondary:hover:not(:disabled) {
  background: var(--bg-soft);
  border-color: var(--ink-soft);
}

.base-btn--ghost {
  background: transparent;
  color: var(--ink-3);
}
.base-btn--ghost:hover:not(:disabled) {
  background: var(--bg-2);
  color: var(--ink);
}

.base-btn--danger {
  background: var(--danger);
  color: white;
}
.base-btn--danger:hover:not(:disabled) {
  background: #B12C3A;
}
</style>
