package sw.chroniq.pages

import sw.platform.api._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import sw.platform.utils.WebUtils._
import java.io.{FileInputStream, File}
import scala.tools.nsc.io._
import spray.http.StatusCodes

class LiveReloadStaticPages extends PartialFunction[PageRequest, Future[PageResponse]] {

  val PUBLICPATH = "src/main/webapp/public"
  val public = new File(PUBLICPATH)

  if (!public.isDirectory) throw new Exception("Search of webapp directory failed")

  def apply(x: PageRequest): Future[PageResponse] = {

    val path = if (x.path == "/") "/index.html" else x.path
    val ct = cType(path)
    val bytes = if ((new File(PUBLICPATH + path)).exists()) {
      Streamable.bytes(new FileInputStream(PUBLICPATH + path))
    } else if ((new File(PUBLICPATH + path + ".html")).exists()) {
      Streamable.bytes(new FileInputStream(PUBLICPATH + path+ ".html"))
    } else "Unknown resource!".getBytes()

    val resp = PageResponse(bytes, contentType = ct)
    Future(resp)

  }

  def isDefinedAt(x: PageRequest): Boolean = {
    (new File(PUBLICPATH + x.path)).exists() || (new File(PUBLICPATH + x.path + ".html")).exists()
  }
}


class StaticPagesFromJar extends PartialFunction[PageRequest, Future[PageResponse]] {

  val files = templatesFromClass

  def apply(x: PageRequest): Future[PageResponse] = {

    println("Request :" + x.path)

    val (ct, ab) = files(x.path)

    Future(PageResponse(ab, contentType = ct))

  }

  def isDefinedAt(x: PageRequest): Boolean = files.isDefinedAt(x.path)
}


class NoSuchPage extends PartialFunction[PageRequest, Future[PageResponse]] {

  def apply(v1: PageRequest): Future[PageResponse] = Future(PageResponse("Not found".getBytes(),status = StatusCodes.NotFound))

  def isDefinedAt(x: PageRequest): Boolean = true

}