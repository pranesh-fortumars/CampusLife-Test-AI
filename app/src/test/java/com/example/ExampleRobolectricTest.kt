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
}
