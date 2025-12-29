<template>
  <div class="register-container">
    <div class="register-form">
      <h2>用户注册</h2>
      <form @submit.prevent="register">
        <div class="form-group">
          <label>用户名</label>
          <input v-model="username" type="text" required>
        </div>
        <div class="form-group">
          <label>密码</label>
          <input v-model="password" type="password" required>
        </div>
        <div class="form-group">
          <label>确认密码</label>
          <input v-model="confirmPassword" type="password" required>
        </div>
        <div class="form-group">
          <label>用户类型</label>
          <select v-model="role" required>
            <option value="">选择用户类型</option>
            <option value="student">学生</option>
            <option value="teacher">教师</option>
          </select>
        </div>
        <button type="submit" class="btn">注册</button>
        <router-link to="/login" class="link">已有账户？登录</router-link>
      </form>
      <p v-if="error" class="error">{{ error }}</p>
      <p v-if="success" class="success">{{ success }}</p>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'Register',
  data() {
    return {
      username: '',
      password: '',
      confirmPassword: '',
      role: '',
      error: '',
      success: ''
    }
  },
  methods: {
    async register() {
      if (this.password !== this.confirmPassword) {
        this.error = '两次输入的密码不一致'
        return
      }
      try {
        await axios.post('/api/users/register', {
          username: this.username,
          password: this.password,
          role: this.role,
          status: 'active'
        })
        this.success = '注册成功！请登录'
        this.username = ''
        this.password = ''
        this.confirmPassword = ''
        this.role = ''
        setTimeout(() => this.$router.push('/login'), 2000)
      } catch (err) {
        this.error = '注册失败，用户名可能已存在'
      }
    }
  }
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 60vh;
}

.register-form {
  width: 100%;
  max-width: 400px;
  padding: 2rem;
  border: 1px solid #ddd;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.register-form h2 {
  text-align: center;
  margin-bottom: 1.5rem;
  color: #2c3e50;
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: bold;
  color: #333;
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

.form-group input:focus,
.form-group select:focus {
  outline: none;
  border-color: #3498db;
  box-shadow: 0 0 5px rgba(52, 152, 219, 0.3);
}

.btn {
  width: 100%;
  padding: 0.75rem;
  background-color: #27ae60;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
  transition: background-color 0.3s;
}

.btn:hover {
  background-color: #229954;
}

.link {
  display: block;
  text-align: center;
  margin-top: 1rem;
  color: #3498db;
  text-decoration: none;
}

.link:hover {
  text-decoration: underline;
}

.error {
  color: #e74c3c;
  text-align: center;
  margin-top: 1rem;
}

.success {
  color: #27ae60;
  text-align: center;
  margin-top: 1rem;
}
</style>