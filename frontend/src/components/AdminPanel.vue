<template>
  <div class="admin-container">
    <h1>📊 管理员控制面板</h1>
    
    <div class="tabs">
      <button 
        v-for="tab in tabs" 
        :key="tab" 
        @click="activeTab = tab"
        :class="{ active: activeTab === tab }"
        class="tab-btn"
      >
        {{ tab }}
      </button>
    </div>

    <!-- 统计报表 -->
    <div v-if="activeTab === '📈 统计报表'" class="statistics-section">
      <div class="stats-grid">
        <div class="stat-card">
          <h3>👥 总用户数</h3>
          <p class="stat-value">{{ statistics.totalUsers }}</p>
        </div>
        <div class="stat-card">
          <h3>📚 总书籍数</h3>
          <p class="stat-value">{{ statistics.totalBooks }}</p>
        </div>
        <div class="stat-card">
          <h3>📖 总借阅数</h3>
          <p class="stat-value">{{ statistics.totalBorrowings }}</p>
        </div>
        <div class="stat-card">
          <h3>⏰ 逾期记录</h3>
          <p class="stat-value overdue">{{ statistics.overdueCount }}</p>
        </div>
        <div class="stat-card">
          <h3>🔄 活跃借阅</h3>
          <p class="stat-value">{{ statistics.activeBorrowings }}</p>
        </div>
      </div>

      <h2 style="margin-top: 2.5rem; margin-bottom: 1rem;">🏆 热门图书排行 TOP 10</h2>
      <table v-if="popularBooks.length > 0" class="data-table">
        <thead>
          <tr>
            <th>#</th>
            <th>书名</th>
            <th>借阅次数</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(book, index) in popularBooks" :key="book.bookId">
            <td>{{ index + 1 }}</td>
            <td>{{ book.title }}</td>
            <td class="center"><span class="badge">{{ book.borrowCount }}</span></td>
          </tr>
        </tbody>
      </table>
      <p v-else class="no-data">暂无数据</p>

      <h2 style="margin-top: 2.5rem; margin-bottom: 1rem;">👤 用户借阅排行</h2>
      <table v-if="userStats.length > 0" class="data-table">
        <thead>
          <tr>
            <th>#</th>
            <th>用户名</th>
            <th>借阅次数</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(user, index) in userStats" :key="user.userId">
            <td>{{ index + 1 }}</td>
            <td>{{ user.username }}</td>
            <td class="center"><span class="badge badge-info">{{ user.borrowCount }}</span></td>
          </tr>
        </tbody>
      </table>
      <p v-else class="no-data">暂无数据</p>
    </div>

    <!-- 图书管理 -->
    <div v-if="activeTab === '📚 图书管理'" class="books-section">
      <button @click="showAddBookForm = !showAddBookForm" class="btn btn-primary btn-lg">
        {{ showAddBookForm ? '✕ 取消' : '➕ 添加新书' }}
      </button>

      <div v-if="showAddBookForm" class="form-container">
        <h3>添加新图书</h3>
        <div class="form-grid">
          <input v-model="newBook.title" placeholder="书名 *" required />
          <input v-model="newBook.author" placeholder="作者 *" required />
          <input v-model="newBook.isbn" placeholder="ISBN *" required />
          <input v-model="newBook.category" placeholder="分类 *" required />
          <input v-model.number="newBook.stock" placeholder="库存数量" type="number" />
          <input v-model="newBook.location" placeholder="书架位置" />
        </div>
        <div class="form-buttons">
          <button @click="addBook" class="btn btn-success">💾 保存</button>
          <button @click="showAddBookForm = false" class="btn btn-secondary">✕ 取消</button>
        </div>
      </div>

      <div class="search-box">
        <input v-model="bookSearchKeyword" placeholder="搜索书名、作者或ISBN..." />
      </div>

      <table v-if="filteredBooks.length > 0" class="data-table">
        <thead>
          <tr>
            <th>书名</th>
            <th>作者</th>
            <th>ISBN</th>
            <th>分类</th>
            <th>库存</th>
            <th>位置</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="book in filteredBooks" :key="book.bookId">
            <td>{{ book.title }}</td>
            <td>{{ book.author }}</td>
            <td>{{ book.isbn }}</td>
            <td>{{ book.category }}</td>
            <td><span class="badge" :class="book.stock === 0 ? 'badge-danger' : 'badge-success'">{{ book.stock }}</span></td>
            <td>{{ book.location }}</td>
            <td class="action-buttons">
              <button @click="editBook(book)" class="btn btn-sm btn-info">✏️ 编辑</button>
              <button @click="deleteBook(book.bookId)" class="btn btn-sm btn-danger">🗑️ 删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-else class="no-data">暂无图书数据</p>
    </div>

    <!-- 用户管理 -->
    <div v-if="activeTab === '👥 用户管理'" class="users-section">
      <div class="search-box">
        <input v-model="userSearchKeyword" placeholder="搜索用户名..." />
      </div>

      <table v-if="filteredUsers.length > 0" class="data-table">
        <thead>
          <tr>
            <th>用户名</th>
            <th>角色</th>
            <th>状态</th>
            <th>注册时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in filteredUsers" :key="user.userId">
            <td>{{ user.username }}</td>
            <td><span class="badge" :class="'badge-' + getRoleClass(user.role)">{{ user.role }}</span></td>
            <td>
              <select @change="updateUserStatus(user, $event)" :value="user.status" class="status-select">
                <option value="ACTIVE">✓ 激活</option>
                <option value="BANNED">✗ 禁用</option>
              </select>
            </td>
            <td>{{ formatDate(user.createdAt) }}</td>
            <td class="action-buttons">
              <button @click="deleteUser(user.userId)" class="btn btn-sm btn-danger">🗑️ 删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-else class="no-data">暂无用户数据</p>
    </div>

    <!-- 借阅记录 -->
    <div v-if="activeTab === '📋 借阅记录'" class="borrowings-section">
      <div class="tabs-sub">
        <button 
          @click="borrowingTab = 'all'"
          :class="{ active: borrowingTab === 'all' }"
          class="subtab-btn"
        >
          所有记录 ({{ allBorrowings.length }})
        </button>
        <button 
          @click="borrowingTab = 'active'"
          :class="{ active: borrowingTab === 'active' }"
          class="subtab-btn"
        >
          活跃借阅 ({{ activeBorrowings.length }})
        </button>
        <button 
          @click="borrowingTab = 'overdue'"
          :class="{ active: borrowingTab === 'overdue' }"
          class="subtab-btn"
        >
          逾期记录 ({{ overdueRecords.length }})
        </button>
      </div>

      <!-- 所有记录 -->
      <table v-if="borrowingTab === 'all' && allBorrowings.length > 0" class="data-table">
        <thead>
          <tr>
            <th>用户</th>
            <th>书名</th>
            <th>借书日期</th>
            <th>应还日期</th>
            <th>还书日期</th>
            <th>逾期费用</th>
            <th>状态</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="b in allBorrowings" :key="b.borrowingId">
            <td>{{ b.user?.username || 'N/A' }}</td>
            <td>{{ b.book?.title || 'N/A' }}</td>
            <td>{{ formatDate(b.borrowDate) }}</td>
            <td>{{ formatDate(b.dueDate) }}</td>
            <td>{{ b.returnDate ? formatDate(b.returnDate) : '-' }}</td>
            <td>{{ b.fine > 0 ? '¥' + b.fine : '-' }}</td>
            <td><span class="badge" :class="b.returnDate ? 'badge-success' : 'badge-warning'">{{ b.returnDate ? '已还' : '借出中' }}</span></td>
          </tr>
        </tbody>
      </table>

      <!-- 活跃借阅 -->
      <table v-else-if="borrowingTab === 'active' && activeBorrowings.length > 0" class="data-table">
        <thead>
          <tr>
            <th>用户</th>
            <th>书名</th>
            <th>借书日期</th>
            <th>应还日期</th>
            <th>剩余天数</th>
            <th>状态</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="b in activeBorrowings" :key="b.borrowingId">
            <td>{{ b.user?.username || 'N/A' }}</td>
            <td>{{ b.book?.title || 'N/A' }}</td>
            <td>{{ formatDate(b.borrowDate) }}</td>
            <td>{{ formatDate(b.dueDate) }}</td>
            <td class="center"><span class="badge" :class="getDaysClass(b.dueDate)">{{ calculateDaysLeft(b.dueDate) }}</span></td>
            <td>
              <span v-if="isOverdue(b.dueDate)" class="badge badge-danger">逾期</span>
              <span v-else class="badge badge-success">正常</span>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- 逾期记录 -->
      <table v-else-if="borrowingTab === 'overdue' && overdueRecords.length > 0" class="data-table">
        <thead>
          <tr>
            <th>用户</th>
            <th>书名</th>
            <th>应还日期</th>
            <th>逾期天数</th>
            <th>应缴费用</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="b in overdueRecords" :key="b.borrowingId">
            <td>{{ b.user?.username || 'N/A' }}</td>
            <td>{{ b.book?.title || 'N/A' }}</td>
            <td>{{ formatDate(b.dueDate) }}</td>
            <td class="center"><span class="badge badge-danger">{{ calculateOverdueDays(b.dueDate) }}</span></td>
            <td>¥{{ (calculateOverdueDays(b.dueDate) * 0.5).toFixed(2) }}</td>
          </tr>
        </tbody>
      </table>

      <p v-if="!allBorrowings.length" class="no-data">暂无借阅记录</p>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'AdminPanel',
  data() {
    return {
      activeTab: '📈 统计报表',
      borrowingTab: 'all',
      tabs: ['📈 统计报表', '📚 图书管理', '👥 用户管理', '📋 借阅记录'],
      statistics: {
        totalUsers: 0,
        totalBooks: 0,
        totalBorrowings: 0,
        overdueCount: 0,
        activeBorrowings: 0,
      },
      popularBooks: [],
      userStats: [],
      books: [],
      users: [],
      allBorrowings: [],
      overdueRecords: [],
      newBook: {
        title: '',
        author: '',
        isbn: '',
        category: '',
        stock: 0,
        location: '',
      },
      showAddBookForm: false,
      bookSearchKeyword: '',
      userSearchKeyword: '',
    };
  },
  computed: {
    filteredBooks() {
      return this.books.filter(book =>
        book.title.toLowerCase().includes(this.bookSearchKeyword.toLowerCase()) ||
        book.author.toLowerCase().includes(this.bookSearchKeyword.toLowerCase()) ||
        book.isbn.includes(this.bookSearchKeyword)
      );
    },
    filteredUsers() {
      return this.users.filter(user =>
        user.username.toLowerCase().includes(this.userSearchKeyword.toLowerCase())
      );
    },
    activeBorrowings() {
      return this.allBorrowings.filter(b => !b.returnDate);
    },
  },
  created() {
    this.checkAdminRole();
    this.loadAllData();
  },
  methods: {
    checkAdminRole() {
      const role = localStorage.getItem('role');
      if (role !== 'admin') {
        alert('只有管理员可以访问此页面');
        this.$router.push('/');
      }
    },
    async loadAllData() {
      await Promise.all([
        this.loadStatistics(),
        this.loadBooks(),
        this.loadUsers(),
        this.loadBorrowings(),
      ]);
    },
    async loadStatistics() {
      try {
        const response = await axios.get('/api/admin/statistics');
        this.statistics = response.data;
        const popularResponse = await axios.get('/api/admin/popular-books');
        this.popularBooks = popularResponse.data;
        const userStatsResponse = await axios.get('/api/admin/user-borrowing-stats');
        this.userStats = userStatsResponse.data;
      } catch (error) {
        console.error('加载统计信息失败', error);
      }
    },
    async loadBooks() {
      try {
        const response = await axios.get('/api/admin/books');
        this.books = response.data;
      } catch (error) {
        console.error('加载图书列表失败', error);
      }
    },
    async loadUsers() {
      try {
        const response = await axios.get('/api/admin/users');
        this.users = response.data;
      } catch (error) {
        console.error('加载用户列表失败', error);
      }
    },
    async loadBorrowings() {
      try {
        const response = await axios.get('/api/admin/borrowing-records');
        this.allBorrowings = response.data;
        const overdueResponse = await axios.get('/api/admin/overdue-records');
        this.overdueRecords = overdueResponse.data;
      } catch (error) {
        console.error('加载借阅记录失败', error);
      }
    },
    async addBook() {
      if (!this.newBook.title || !this.newBook.author || !this.newBook.isbn || !this.newBook.category) {
        alert('请填写必填项');
        return;
      }
      try {
        await axios.post('/api/admin/books', this.newBook);
        this.showAddBookForm = false;
        this.newBook = { title: '', author: '', isbn: '', category: '', stock: 0, location: '' };
        await this.loadBooks();
        alert('✓ 图书添加成功');
      } catch (error) {
        alert('❌ 添加失败：' + (error.response?.data?.message || error.message));
      }
    },
    async deleteBook(id) {
      if (confirm('确定要删除此图书吗？')) {
        try {
          await axios.delete(`/api/admin/books/${id}`);
          await this.loadBooks();
          alert('✓ 删除成功');
        } catch (error) {
          alert('❌ 删除失败');
        }
      }
    },
    editBook(book) {
      alert('编辑功能开发中');
    },
    async updateUserStatus(user, event) {
      try {
        await axios.put(`/api/admin/users/${user.userId}/status`, null, {
          params: { status: event.target.value }
        });
        user.status = event.target.value;
        alert('✓ 用户状态已更新');
      } catch (error) {
        alert('❌ 更新失败');
      }
    },
    async deleteUser(userId) {
      if (confirm('确定要删除此用户吗？')) {
        try {
          await axios.delete(`/api/admin/users/${userId}`);
          await this.loadUsers();
          alert('✓ 删除成功');
        } catch (error) {
          alert('❌ 删除失败');
        }
      }
    },
    formatDate(dateString) {
      if (!dateString) return '-';
      const date = new Date(dateString);
      return date.toLocaleDateString('zh-CN');
    },
    getRoleClass(role) {
      const roleMap = {
        'admin': 'danger',
        'teacher': 'warning',
        'student': 'info'
      };
      return roleMap[role.toLowerCase()] || 'secondary';
    },
    calculateDaysLeft(dueDate) {
      const due = new Date(dueDate);
      const now = new Date();
      const daysLeft = Math.ceil((due - now) / (1000 * 60 * 60 * 24));
      return daysLeft;
    },
    calculateOverdueDays(dueDate) {
      const due = new Date(dueDate);
      const now = new Date();
      const daysOverdue = Math.ceil((now - due) / (1000 * 60 * 60 * 24));
      return Math.max(0, daysOverdue);
    },
    isOverdue(dueDate) {
      return new Date(dueDate) < new Date();
    },
    getDaysClass(dueDate) {
      const daysLeft = this.calculateDaysLeft(dueDate);
      if (daysLeft < 0) return 'badge-danger';
      if (daysLeft < 3) return 'badge-warning';
      return 'badge-success';
    },
  },
};
</script>

<style scoped>
.admin-container {
  padding: 2rem;
  max-width: 1400px;
  margin: 0 auto;
  background: #f8f9fa;
  border-radius: 8px;
}

.admin-container h1 {
  color: #2c3e50;
  margin-bottom: 2rem;
  font-size: 2rem;
}

.tabs {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 2rem;
  flex-wrap: wrap;
  border-bottom: 2px solid #e0e0e0;
}

.tab-btn {
  padding: 0.75rem 1.5rem;
  border: none;
  background: transparent;
  cursor: pointer;
  font-size: 1rem;
  font-weight: 500;
  color: #666;
  border-bottom: 3px solid transparent;
  transition: all 0.3s;
}

.tab-btn:hover {
  color: #3498db;
}

.tab-btn.active {
  color: #3498db;
  border-bottom-color: #3498db;
}

.tabs-sub {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
  border-bottom: 1px solid #ddd;
}

.subtab-btn {
  padding: 0.5rem 1rem;
  border: none;
  background: transparent;
  cursor: pointer;
  color: #666;
  border-bottom: 2px solid transparent;
  transition: all 0.3s;
}

.subtab-btn:hover {
  color: #3498db;
}

.subtab-btn.active {
  color: #3498db;
  border-bottom-color: #3498db;
}

.statistics-section {
  animation: fadeIn 0.3s ease-in;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.stat-card {
  background: white;
  padding: 1.5rem;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  text-align: center;
  border-left: 4px solid #3498db;
}

.stat-card h3 {
  color: #666;
  font-size: 0.9rem;
  margin: 0 0 0.75rem 0;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.stat-value {
  font-size: 2.5rem;
  font-weight: bold;
  color: #3498db;
  margin: 0;
}

.stat-card .overdue {
  color: #e74c3c;
}

.form-container {
  background: white;
  padding: 2rem;
  border-radius: 8px;
  margin-bottom: 2rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.form-container h3 {
  color: #2c3e50;
  margin-top: 0;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.form-grid input {
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 0.9rem;
}

.form-grid input:focus {
  outline: none;
  border-color: #3498db;
  box-shadow: 0 0 5px rgba(52, 152, 219, 0.3);
}

.form-buttons {
  display: flex;
  gap: 1rem;
}

.search-box {
  margin-bottom: 1.5rem;
}

.search-box input {
  width: 100%;
  max-width: 400px;
  padding: 0.75rem 1rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 0.9rem;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 2rem;
}

.data-table thead {
  background: #f5f5f5;
  font-weight: bold;
}

.data-table th,
.data-table td {
  padding: 1rem;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.data-table tr:hover {
  background: #f9f9f9;
}

.data-table .center {
  text-align: center;
}

.action-buttons {
  display: flex;
  gap: 0.5rem;
}

.btn {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: 500;
  transition: all 0.3s;
}

.btn-primary {
  background: #3498db;
  color: white;
}

.btn-primary:hover {
  background: #2980b9;
}

.btn-secondary {
  background: #95a5a6;
  color: white;
}

.btn-secondary:hover {
  background: #7f8c8d;
}

.btn-success {
  background: #27ae60;
  color: white;
}

.btn-success:hover {
  background: #229954;
}

.btn-danger {
  background: #e74c3c;
  color: white;
}

.btn-danger:hover {
  background: #c0392b;
}

.btn-info {
  background: #3498db;
  color: white;
}

.btn-info:hover {
  background: #2980b9;
}

.btn-sm {
  padding: 0.35rem 0.75rem;
  font-size: 0.8rem;
}

.btn-lg {
  padding: 0.75rem 2rem;
  font-size: 1rem;
  margin-bottom: 1.5rem;
}

.badge {
  display: inline-block;
  padding: 0.35rem 0.75rem;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 500;
}

.badge-success {
  background: #d4edda;
  color: #155724;
}

.badge-danger {
  background: #f8d7da;
  color: #721c24;
}

.badge-warning {
  background: #fff3cd;
  color: #856404;
}

.badge-info {
  background: #d1ecf1;
  color: #0c5460;
}

.status-select {
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: white;
  cursor: pointer;
}

.no-data {
  text-align: center;
  color: #95a5a6;
  padding: 2rem;
  font-size: 1rem;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}
</style>
