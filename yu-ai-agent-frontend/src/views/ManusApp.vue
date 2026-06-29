<template>
  <div class="page chat-page">
    <header class="topbar">
      <router-link to="/">← 返回</router-link>
      <h2>AI 超级智能体</h2>
    </header>

    <main class="chat-container" ref="container">
      <div v-for="(m, idx) in messages" :key="idx" :class="['msg', m.role === 'user' ? 'right' : 'left']">
        <div class="bubble">{{ m.text }}</div>
      </div>
    </main>

    <footer class="composer">
      <input v-model="inputMessage" @keyup.enter="sendMessage" placeholder="请输入内容，回车发送" />
      <button @click="sendMessage">发送</button>
    </footer>
  </div>
</template>

<script>
import { ref, onMounted, nextTick } from 'vue'
import axios from 'axios'

export default {
  name: 'ManusApp',
  setup() {
    const messages = ref([])
    const inputMessage = ref('')
    const container = ref(null)

    function scrollToBottom() {
      nextTick(() => {
        if (container.value) container.value.scrollTop = container.value.scrollHeight
      })
    }

    function sendMessage() {
      const msg = inputMessage.value && inputMessage.value.trim()
      if (!msg) return
      messages.value.push({ role: 'user', text: msg })
      inputMessage.value = ''
      scrollToBottom()

      const base = axios.defaults.baseURL || ''
      const url = `${base.replace(/\/$/, '')}/ai/manus/chat?message=${encodeURIComponent(msg)}`

      const es = new EventSource(url)
      let aiIndex = -1

      es.onmessage = (e) => {
        const data = e.data || ''
        if (aiIndex === -1) {
          messages.value.push({ role: 'ai', text: data })
          aiIndex = messages.value.length - 1
        } else {
          messages.value[aiIndex].text += data
        }
        scrollToBottom()
      }

      es.onerror = () => {
        try { es.close() } catch (err) {}
      }
    }

    onMounted(() => {
      messages.value.push({ role: 'ai', text: '已连接 AI 超级智能体，随时发问。' })
      scrollToBottom()
    })

    return { messages, inputMessage, sendMessage, container }
  }
}
</script>
