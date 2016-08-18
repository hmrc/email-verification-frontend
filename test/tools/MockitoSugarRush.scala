package tools

import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar

trait MockitoSugarRush extends MockitoSugar {
  override def mock[T <: AnyRef](implicit manifest: Manifest[T]) = super.mock(defaultAnswer = RETURNS_SMART_NULLS)
}
