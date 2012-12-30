package com.geeksville.util

/**
 * C++ style using
 * http://stackoverflow.com/questions/2207425/what-automatic-resource-management-alternatives-exists-for-scala
 *
 * Example:
 *
 * using(new BufferedReader(new FileReader("file"))) { r =>
 * var count = 0
 * while (r.readLine != null) count += 1
 * println(count)
 * }
 */
object Using {

  def using[T <: { def close() }, ResType](resource: T)(block: T => ResType) =
    {
      try {
        block(resource)
      } finally {
        if (resource != null) resource.close()
      }
    }
}
