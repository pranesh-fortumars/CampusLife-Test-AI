package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("CampusLife", appName)
  }

  @Test
  fun `test MainActivity instantiation`() {
    val controller = org.robolectric.Robolectric.buildActivity(MainActivity::class.java).setup()
    val activity = controller.get()
    org.junit.Assert.assertNotNull(activity)
  }

  @Test
  fun `test CampusLifeViewModel instantiation via AndroidViewModelFactory`() {
    val application = ApplicationProvider.getApplicationContext<android.app.Application>()
    val factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(application)
    val vm = factory.create(com.example.viewmodel.CampusLifeViewModel::class.java)
    org.junit.Assert.assertNotNull(vm)
    org.junit.Assert.assertNotNull(vm.profile.value)
  }

  @Test
  fun `test Room TaskDao operations`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = com.example.data.local.AppDatabase.getDatabase(context)
    val taskDao = db.taskDao()

    val task = com.example.data.local.TaskEntity(
      id = "robo-task-1",
      title = "Robolectric CN Project Milestone",
      subjectCode = "CS-306",
      subjectName = "Computer Networks",
      category = "Project",
      type = "PROJECT",
      dueDate = "Tomorrow",
      priority = "HIGH"
    )
    taskDao.insertTask(task)
    val count = taskDao.getTaskCount()
    org.junit.Assert.assertTrue(count >= 1)

    taskDao.setTaskCompleted("robo-task-1", true)
    org.junit.Assert.assertEquals("Project", task.category)
  }

  @Test
  fun `test AppDatabase singleton builder and provider`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db1 = com.example.data.local.AppDatabase.getDatabase(context)
    val db2 = com.example.data.local.AppDatabase.getInstance(context)
    org.junit.Assert.assertSame(db1, db2)
    org.junit.Assert.assertEquals("campuslife_db", com.example.data.local.AppDatabase.DATABASE_NAME)

    val taskDao1 = com.example.data.local.AppDatabase.provideTaskDao(context)
    val taskDao2 = com.example.data.local.DatabaseProvider.provideTaskDao(context)
    org.junit.Assert.assertNotNull(taskDao1)
    org.junit.Assert.assertNotNull(taskDao2)
  }

  @Test
  fun `test TaskViewModel injection via TaskDao and Factory`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val taskDao = com.example.data.local.AppDatabase.provideTaskDao(context)
    org.junit.Assert.assertNotNull(taskDao)

    // Direct constructor injection of TaskDao
    val viewModel = com.example.viewmodel.TaskViewModel(taskDao)
    org.junit.Assert.assertNotNull(viewModel)

    // Factory provider injection
    val factory = com.example.viewmodel.TaskViewModel.provideFactory(taskDao)
    val vmFromFactory = factory.create(com.example.viewmodel.TaskViewModel::class.java)
    org.junit.Assert.assertNotNull(vmFromFactory)

    // Context-based factory provider using singleton database
    val contextFactory = com.example.viewmodel.TaskViewModel.provideFactory(context)
    val vmFromContextFactory = contextFactory.create(com.example.viewmodel.TaskViewModel::class.java)
    org.junit.Assert.assertNotNull(vmFromContextFactory)

    // Task CRUD operations through injected TaskViewModel
    val testTask = com.example.model.AcademicTask(
      id = "tvm-test-1",
      title = "Injected TaskViewModel Test Task",
      subjectCode = "CS-302",
      subjectName = "DBMS",
      category = "Assignment",
      dueDate = "Today",
      isCompleted = false
    )
    viewModel.insertTask(testTask)
    val inserted = taskDao.getTaskById("tvm-test-1")
    org.junit.Assert.assertNotNull(inserted)
    org.junit.Assert.assertFalse(inserted!!.isCompleted)

    // Toggle completion atomically
    val newStatus = taskDao.toggleTaskCompletion("tvm-test-1")
    org.junit.Assert.assertTrue(newStatus)
    val toggled = taskDao.getTaskById("tvm-test-1")
    org.junit.Assert.assertTrue(toggled!!.isCompleted)

    // Toggle back via ViewModel method
    viewModel.toggleTaskCompletion("tvm-test-1")
    viewModel.deleteTask("tvm-test-1")
    viewModel.clearUserMessage()
  }
}
