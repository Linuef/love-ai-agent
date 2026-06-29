<template>
  <div class="page chat-page theme-manus">
    <header class="topbar">
      <router-link to="/">← 返回</router-link>
      <h2>AI 超级智能体</h2>
    </header>

    <main class="chat-container" ref="container">
      <div v-for="(m, idx) in messages" :key="idx" :class="['msg', m.role === 'user' ? 'right' : 'left']">
        <div v-if="m.role === 'ai'" class="avatar-small">🤖</div>
        <div class="bubble">
          <template v-if="m.role === 'ai'">
            <p v-for="(p, pi) in (m.paragraphs || [])" :key="pi">{{ p }}</p>
          </template>
          <template v-else>
            <p>{{ m.text }}</p>
          </template>
        </div>
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

      es.onmessage = async (e) => {
        const data = e.data || ''
        if (aiIndex === -1) {
          const paragraphs = []
          messages.value.push({ role: 'ai', text: data + '\n\n', paragraphs })
          aiIndex = messages.value.length - 1
        } else {
          messages.value[aiIndex].text += data + '\n\n'
        }
        try {
          const { parseStructured } = await import('../utils/parseResponse.js')
          messages.value[aiIndex].paragraphs = parseStructured(messages.value[aiIndex].text)
        } catch (err) {
          messages.value[aiIndex].paragraphs = [messages.value[aiIndex].text]
        }
        scrollToBottom()
      }

      es.onerror = () => {
        try { es.close() } catch (err) {}
      }
    }

    onMounted(() => {
      messages.value.push({ role: 'ai', text: '已连接 AI 超级智能体，随时发问。', paragraphs: ['已连接 AI 超级智能体，随时发问。'] })
      scrollToBottom()
    })

    return { messages, inputMessage, sendMessage, container }
  }
}
</script>
