package net.ketc.numeri

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(JUnitPlatform::class)
class ExampleSpec : Spek({
    describe("a addition") {
        it("2+2 equals 4") {
            assertEquals(4, 2 + 2)
        }
    }

})