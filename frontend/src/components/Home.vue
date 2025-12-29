<template>
  <div class="home-container">
    <div class="welcome-section">
      <h1>欢迎来到图书管理系统</h1>
      <p>一个现代化的图书馆藏借阅管理平台</p>
    </div>
    <div class="features-grid">
      <router-link to="/search" class="feature-card">
        <div class="icon">📚</div>
        <h3>图书检索</h3>
        <p>快速搜索馆藏图书，查看详细信息</p>
      </router-link>
      <router-link to="/borrowings" v-if="isLoggedIn" class="feature-card">
        <div class="icon">📖</div>
        <h3>借阅管理</h3>
        <p>查看借阅历史、续借和归还图书</p>
      </router-link>
      <router-link to="/admin" v-if="isAdmin" class="feature-card">
        <div class="icon">⚙️</div>
        <h3>管理中心</h3>
        <p>维护图书信息、管理用户账户</p>
      </router-link>
      <router-link to="/login" v-if="!isLoggedIn" class="feature-card">
        <div class="icon">🔐</div>
        <h3>用户登录</h3>
        <p>登录您的账户，开始借阅</p>
      </router-link>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Home',
  data() {
    return {
      isLoggedIn: false,
      isAdmin: false
    }
  },
  mounted() {
    this.isLoggedIn = !!localStorage.getItem('token')
    this.isAdmin = localStorage.getItem('role') === 'admin'
  }
}
</script>

<style scoped>
.home-container {
  max-width: 1200px;
  margin: 0 auto;
}

.welcome-section {
  text-align: center;
  padding: 3rem 1rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 8px;
  margin-bottom: 3rem;
}

.welcome-section h1 {
  font-size: 2.5rem;
  margin-bottom: 0.5rem;
}

.welcome-section p {
  font-size: 1.2rem;
  opacity: 0.9;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 2rem;
}

.feature-card {
  background: white;
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 2rem;
  text-align: center;
  text-decoration: none;
  color: #333;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s, box-shadow 0.3s;
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.15);
}

.feature-card .icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.feature-card h3 {
  color: #2c3e50;
  margin-bottom: 0.75rem;
  font-size: 1.3rem;
}

.feature-card p {
  color: #666;
  line-height: 1.6;
}
</style>