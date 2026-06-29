<template>
  <div class="page chat-page">
    <header class="topbar">
      <router-link to="/">← 返回</router-link>
      <h2>AI 恋爱大师</h2>
    </header>

    <main class="chat-container" ref="container">
      <div v-for="(m, idx) in messages" :key="idx" :class="['msg', m.role === 'user' ? 'right' : 'left']">
        <div class="bubble">
          <template v-if="m.role === 'ai'">
            <p v-for="(p, pi) in m.paragraphs" :key="pi">{{ p }}</p>
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
  name: 'LoveApp',
  setup() {
    const messages = ref([])
    const inputMessage = ref('')
    const container = ref(null)
    const chatId = ref(generateChatId())

    function generateChatId() {
      return Date.now().toString(36) + Math.random().toString(36).slice(2, 9)
    }

    function scrollToBottom() {
      nextTick(() => {
        if (container.value) {
          container.value.scrollTop = container.value.scrollHeight
        }
      })
    }

    function sendMessage() {
      const msg = inputMessage.value && inputMessage.value.trim()
      if (!msg) return
      messages.value.push({ role: 'user', text: msg })
      inputMessage.value = ''
      scrollToBottom()

      const base = axios.defaults.baseURL || ''
      const url = `${base.replace(/\/$/, '')}/ai/love_app/chat/sse?message=${encodeURIComponent(msg)}&chatId=${encodeURIComponent(chatId.value)}`

      const es = new EventSource(url)
      let aiIndex = -1

      es.onmessage = async (e) => {
        const data = e.data || ''
        if (aiIndex === -1) {
          // 初次接收时创建 AI 消息对象，包含 raw text 与段落数组
          const paragraphs = []
          messages.value.push({ role: 'ai', text: data, paragraphs })
          aiIndex = messages.value.length - 1
        } else {
          messages.value[aiIndex].text += data
        }
        // 解析并更新段落
        try {
          const { parseStructured } = await import('../utils/parseResponse.js')
          messages.value[aiIndex].paragraphs = parseStructured(messages.value[aiIndex].text)
        } catch (err) {
          // 解析失败回退为整段文本
          messages.value[aiIndex].paragraphs = [messages.value[aiIndex].text]
        }
        scrollToBottom()
      }

      es.onerror = () => {
        try { es.close() } catch (err) {}
      }
    }

    onMounted(() => {
      messages.value.push({ role: 'ai', text: '欢迎使用 AI 恋爱大师，开始聊天吧～', paragraphs: ['欢迎使用 AI 恋爱大师，开始聊天吧～'] })
      scrollToBottom()
    })

    return { messages, inputMessage, sendMessage, container }
  }
}
</script>
