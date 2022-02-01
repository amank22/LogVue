package com.voxfinite.logvue.storage.serializer

import org.mapdb.CC
import org.mapdb.DataInput2
import org.mapdb.DataOutput2
import org.mapdb.serializer.GroupSerializerObjectArray
import java.io.*

class ObjectSerializer<T>(val classLoader: ClassLoader = Thread.currentThread().contextClassLoader) :
    GroupSerializerObjectArray<T>() {

    override fun serialize(out: DataOutput2, value: T) {
        val out2 = ObjectOutputStream(out as OutputStream)
        out2.writeObject(value)
        out2.flush()
    }

    override fun deserialize(input: DataInput2, available: Int): T {
        return try {
            val in2: ObjectInputStream = ObjectInputStreamWithLoader(DataInput2.DataInputToStream(input))
            in2.readObject() as T
        } catch (e: ClassNotFoundException) {
            throw IOException(e)
        } catch (e: ClassCastException) {
            throw IOException(e)
        }
    }

    @Throws(IOException::class)
    override fun valueArrayDeserialize(`in`: DataInput2?, size: Int): Array<Any?>? {
        return try {
            val in2: ObjectInputStream = ObjectInputStreamWithLoader(DataInput2.DataInputToStream(`in`))
            val ret = in2.readObject()
            if (CC.PARANOID && size != valueArraySize(ret)) throw AssertionError()
            ret as Array<Any?>
        } catch (e: ClassNotFoundException) {
            throw IOException(e)
        }
    }

    @Throws(IOException::class)
    override fun valueArraySerialize(out: DataOutput2?, vals: Any?) {
        val out2 = ObjectOutputStream(out as OutputStream?)
        out2.writeObject(vals)
        out2.flush()
    }

    /**
     * This subclass of ObjectInputStream delegates loading of classes to
     * an existing ClassLoader.
     */
    internal inner class ObjectInputStreamWithLoader
    /**
     * Loader must be non-null;
     */
        (`in`: InputStream?) : ObjectInputStream(`in`) {
        /**
         * Use the given ClassLoader rather than using the system class
         */
        @Throws(IOException::class, ClassNotFoundException::class)
        override fun resolveClass(desc: ObjectStreamClass): Class<*> {
            val name = desc.name
            return try {
                Class.forName(name, false, classLoader)
            } catch (ex: ClassNotFoundException) {
                super.resolveClass(desc)
            }
        }
    }
}
