<template>
  <div class="borrowings-container">
    <h2>我的借阅历史</h2>
    <div v-if="borrowings.length > 0" class="borrowings-list">
      <div v-for="borrowing in borrowings" :key="borrowing.borrowingId" class="borrowing-item">
        <div class="borrowing-info">
          <h3>{{ borrowing.book.title }}</h3>
          <p><strong>作者：</strong>{{ borrowing.book.author }}</p>
          <p><strong>借书日期：</strong>{{ formatDate(borrowing.borrowDate) }}</p>
          <p><strong>应还日期：</strong>{{ formatDate(borrowing.dueDate) }}</p>
          <p v-if="borrowing.returnDate"><strong>归还日期：</strong>{{ formatDate(borrowing.returnDate) }}</p>
          <p v-else><strong>状态：</strong><span class="status-borrowed">已借出</span></p>
          <p v-if="borrowing.fine > 0"><strong>逾期费用：</strong>¥{{ borrowing.fine }}</p>
        </div>
        <button v-if="!borrowing.returnDate" @click="returnBook(borrowing.borrowingId)" class="btn return-btn">
          归还
        </button>
        <span v-else class="returned">已归还</span>
      </div>
    </div>
    <p v-else class="no-borrowings">暂无借阅记录</p>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'BorrowingHistory',
  data() {
    return {
      borrowings: []
    }
  },
  methods: {
    async fetchBorrowings() {
      if (!localStorage.getItem('token')) {
        this.$router.push('/login')
        return
      }
      try {
        // 从真实API获取用户ID（这里需要存储用户ID）
        const userId = localStorage.getItem('userId') || 1
        const response = await axios.get(`/api/borrowings/${userId}`)
        this.borrowings = response.data
      } catch (err) {
        console.error('获取借阅记录失败', err)
      }
    },
    async returnBook(borrowingId) {
      try {
        await axios.post('/api/return', null, {
          params: { borrowingId: borrowingId }
        })
        alert('归还成功！')
        this.fetchBorrowings()
      } catch (err) {
        alert('归还失败，请稍后重试')
      }
    },
    formatDate(dateString) {
      const date = new Date(dateString)
      return date.toLocaleDateString('zh-CN')
    }
  },
  mounted() {
    this.fetchBorrowings()
  }
}
</script>

<style scoped>
.borrowings-container {
  max-width: 900px;
  margin: 0 auto;
}

.borrowings-container h2 {
  color: #2c3e50;
  margin-bottom: 1.5rem;
}

.borrowings-list {
  display: grid;
  gap: 1.5rem;
}

.borrowing-item {
  border: 1px solid #ddd;
  padding: 1.5rem;
  border-radius: 8px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.borrowing-info {
  flex: 1;
}

.borrowing-info h3 {
  color: #2c3e50;
  margin-bottom: 0.75rem;
}

.borrowing-info p {
  color: #555;
  margin-bottom: 0.5rem;
  line-height: 1.6;
}

.status-borrowed {
  color: #e74c3c;
  font-weight: bold;
}

.btn {
  padding: 0.75rem 1.5rem;
  background-color: #27ae60;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
  transition: background-color 0.3s;
}

.btn:hover {
  background-color: #229954;
}

.return-btn {
  align-self: center;
}

.returned {
  color: #27ae60;
  font-weight: bold;
  padding: 0.75rem 1.5rem;
  background-color: #f0f0f0;
  border-radius: 4px;
}

.no-borrowings {
  text-align: center;
  color: #95a5a6;
  padding: 2rem;
  font-size: 1.1rem;
}
</style>