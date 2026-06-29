<template>
  <div class="page">
    <div class="header-wrapper">
      <div class="flow-light" v-if="showFlow">
        <span class="light-strip left"></span>
        <span class="light-strip right"></span>
      </div>
      <div class="app-header">
        <div class="neon-title">鱼皮 AI 超级智能体</div>
        <div class="neon-sub">探索AI的无限可能</div>
      </div>
    </div>

    <div class="cards">
      <router-link to="/love" class="card">
        <div class="avatar avatar-love">❤</div>
        <h3>AI 恋爱大师</h3>
        <p>智能情感顾问，帮你解答恋爱烦恼</p>
        <button class="btn-primary">立即体验 →</button>
      </router-link>

      <router-link to="/manus" class="card">
        <div class="avatar avatar-manus">🤖</div>
        <h3>AI 超级智能体</h3>
        <p>全能型 AI 助手，解决各类专业问题</p>
        <button class="btn-primary">立即体验 →</button>
      </router-link>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Home',
  data() {
    return {
      showFlow: true
    }
  },
  mounted() {
    setTimeout(() => {
      this.showFlow = false
    }, 1800)
  }
}
</script>
