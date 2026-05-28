import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import Notes from '../views/Notes.vue'
import Tutor from '../views/Tutor.vue'
import Diagnose from '../views/Diagnose.vue'
import Exam from '../views/Exam.vue'
import Todo from '../views/Todo.vue'

const routes = [
  { path: '/', name: 'Home', component: Home },
  { path: '/notes', name: 'Notes', component: Notes },
  { path: '/tutor', name: 'Tutor', component: Tutor },
  { path: '/diagnose', name: 'Diagnose', component: Diagnose },
  { path: '/exam', name: 'Exam', component: Exam },
  { path: '/todo', name: 'Todo', component: Todo }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
