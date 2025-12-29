<template>
  <div class="search-container">
    <h2>图书检索</h2>
    <div class="search-box">
      <input v-model="keyword" type="text" placeholder="输入书名、作者或ISBN进行查询">
      <button @click="search" class="btn">查询</button>
    </div>
    <div v-if="books.length > 0" class="books-list">
      <div v-for="book in books" :key="book.bookId" class="book-item">
        <div class="book-info">
          <h3>{{ book.title }}</h3>
          <p><strong>作者：</strong>{{ book.author }}</p>
          <p><strong>ISBN：</strong>{{ book.isbn }}</p>
          <p><strong>分类：</strong>{{ book.category }}</p>
          <p><strong>库存：</strong>{{ book.stock }}</p>
          <p><strong>位置：</strong>{{ book.location }}</p>
        </div>
        <button @click="borrowBook(book.bookId)" class="btn borrow-btn" :disabled="book.stock === 0">
          {{ book.stock > 0 ? '借阅' : '无库存' }}
        </button>
      </div>
    </div>
    <p v-else class="no-results">{{ searched ? '未找到相关图书' : '请输入关键词搜索' }}</p>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'BookSearch',
  data() {
    return {
      keyword: '',
      books: [],
      searched: false
    }
  },
  methods: {
    async search() {
      if (!this.keyword.trim()) {
        this.books = []
        this.searched = false
        return
      }
      try {
        const response = await axios.get('/api/books/search', {
          params: { keyword: this.keyword }
        })
        this.books = response.data
        this.searched = true
      } catch (err) {
        this.books = []
        this.searched = true
      }
    },
    async borrowBook(bookId) {
      if (!localStorage.getItem('token')) {
        this.$router.push('/login')
        return
      }
      try {
        await axios.post('/api/borrow', {
          username: localStorage.getItem('username'),
          bookId: bookId
        })
        alert('借阅成功！')
        this.search()
      } catch (err) {
        alert('借阅失败，请稍后重试')
      }
    }
  }
}
</script>

<style scoped>
.search-container {
  max-width: 900px;
  margin: 0 auto;
}

.search-container h2 {
  color: #2c3e50;
  margin-bottom: 1.5rem;
}

.search-box {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
}

.search-box input {
  flex: 1;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

.search-box input:focus {
  outline: none;
  border-color: #3498db;
  box-shadow: 0 0 5px rgba(52, 152, 219, 0.3);
}

.btn {
  padding: 0.75rem 1.5rem;
  background-color: #3498db;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
  transition: background-color 0.3s;
}

.btn:hover:not(:disabled) {
  background-color: #2980b9;
}

.borrow-btn {
  background-color: #27ae60;
  padding: 0.5rem 1rem;
  font-size: 0.9rem;
}

.borrow-btn:hover:not(:disabled) {
  background-color: #229954;
}

.borrow-btn:disabled {
  background-color: #95a5a6;
  cursor: not-allowed;
}

.books-list {
  display: grid;
  gap: 1.5rem;
}

.book-item {
  border: 1px solid #ddd;
  padding: 1.5rem;
  border-radius: 8px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.book-info {
  flex: 1;
}

.book-info h3 {
  color: #2c3e50;
  margin-bottom: 0.75rem;
}

.book-info p {
  color: #555;
  margin-bottom: 0.5rem;
  line-height: 1.6;
}

.no-results {
  text-align: center;
  color: #95a5a6;
  padding: 2rem;
  font-size: 1.1rem;
}
</style>