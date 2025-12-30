<template>
  <div id="app">
    <nav class="navbar">
      <h1>图书管理系统</h1>
      <div class="nav-links">
        <router-link to="/">首页</router-link>
        <router-link to="/search">检索</router-link>
        <router-link to="/borrowings">借阅历史</router-link>
        <router-link to="/admin" v-if="isAdmin">管理</router-link>
        <router-link to="/login" v-if="!isLoggedIn">登录</router-link>
        <button @click="logout" v-if="isLoggedIn">登出</button>
      </div>
    </nav>
    <div class="container">
      <router-view></router-view>
    </div>
  </div>
</template>

<script>
export default {
  name: 'App',
  data() {
    return {
      isLoggedIn: false,
      isAdmin: false
    }
  },
  methods: {
    logout() {
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      localStorage.removeItem('role')
      this.isLoggedIn = false
      this.isAdmin = false
      this.$router.push('/login')
    },
    checkAuth() {
      this.isLoggedIn = !!localStorage.getItem('token')
      this.isAdmin = localStorage.getItem('role') === 'admin'
      console.log('Auth check:', { isLoggedIn: this.isLoggedIn, isAdmin: this.isAdmin, role: localStorage.getItem('role') })
    }
  },
  mounted() {
    this.checkAuth()
    // 监听路由变化
    this.$router.afterEach(() => {
      this.checkAuth()
    })
  }
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Microsoft YaHei', Arial, sans-serif;
}

.navbar {
  background-color: #2c3e50;
  color: white;
  padding: 1rem 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.navbar h1 {
  font-size: 1.5rem;
}

.nav-links {
  display: flex;
  gap: 1.5rem;
  align-items: center;
}

.nav-links a {
  color: white;
  text-decoration: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.nav-links a:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.container {
  max-width: 1200px;
  margin: 2rem auto;
  padding: 0 1rem;
}

button {
  background-color: #e74c3c;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

button:hover {
  background-color: #c0392b;
}
</style>