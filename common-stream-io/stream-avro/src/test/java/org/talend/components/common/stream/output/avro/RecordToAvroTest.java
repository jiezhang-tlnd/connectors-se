/*
 * Copyright (C) 2006-2020 Talend Inc. - www.talend.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.talend.components.common.stream.output.avro;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;

import org.apache.avro.generic.GenericRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.talend.components.common.stream.input.avro.AvroToRecord;
import org.talend.sdk.component.api.record.Record;
import org.talend.sdk.component.api.record.Schema;
import org.talend.sdk.component.api.record.Schema.Entry;
import org.talend.sdk.component.api.record.Schema.Type;
import org.talend.sdk.component.api.service.record.RecordBuilderFactory;
import org.talend.sdk.component.runtime.record.RecordBuilderFactoryImpl;

class RecordToAvroTest {

    protected Record versatileRecord;

    protected Record complexRecord;

    private RecordBuilderFactory factory = new RecordBuilderFactoryImpl("test");

    private ZonedDateTime now = ZonedDateTime.now();

    @BeforeEach
    protected void setUp() throws Exception {
        this.prepareTestRecords();
    }

    @Test
    void withNullSubrecord() {
        final RecordToAvro converter = new RecordToAvro("test");

        final Entry entryField1 = factory.newEntryBuilder() //
                .withType(Type.STRING) //
                .withNullable(true) //
                .withName("field") //
                .build();
        final Schema subRecordSchema = factory //
                .newSchemaBuilder(Type.RECORD) //
                .withEntry(entryField1) //
                .build();
        final Record subRecord = factory.newRecordBuilder(subRecordSchema) //
                .withString(entryField1, "Hello") //
                .build();

        final Entry entryRecord = factory.newEntryBuilder() //
                .withType(Type.RECORD) //
                .withNullable(true) //
                .withName("sub") //
                .withElementSchema(subRecordSchema) //
                .build();

        final Schema recordSchema = factory //
                .newSchemaBuilder(Type.RECORD) //
                .withEntry(entryRecord) //
                .build();

        final Record firstRecord = factory.newRecordBuilder(recordSchema) //
                .withRecord(entryRecord, subRecord) //
                .build();
        final GenericRecord genericRecord1 = converter.fromRecord(firstRecord);
        Assertions.assertNotNull(genericRecord1);
        final Object sub = genericRecord1.get("sub");
        Assertions.assertNotNull("sub");
        Assertions.assertTrue(sub instanceof GenericRecord, "sub is of " + sub.getClass().getName());

        final Record secondRecord = factory.newRecordBuilder(recordSchema) //
                .build();
        final GenericRecord genericRecord2 = converter.fromRecord(secondRecord);
        Assertions.assertNotNull(genericRecord2);

    }

    @Test
    void withArrayOfRecord() {
        final RecordToAvro converter = new RecordToAvro("test");

        final Entry entryField1 = factory.newEntryBuilder() //
                .withType(Type.BYTES) //
                .withNullable(true) //
                .withName("field") //
                .build();
        final Schema subRecordSchema = factory //
                .newSchemaBuilder(Type.RECORD) //
                .withEntry(entryField1) //
                .build();
        final Record subRecord1 = factory.newRecordBuilder(subRecordSchema) //
                .withBytes(entryField1, "Hello1".getBytes()) //
                .build();
        final Record subRecord2 = factory.newRecordBuilder(subRecordSchema) //
                .withBytes(entryField1, "Hello2".getBytes()) //
                .build();
        final Record subRecord3 = factory.newRecordBuilder(subRecordSchema) //
                .build();

        final Entry entryRecord = factory.newEntryBuilder() //
                .withType(Type.ARRAY) //
                .withNullable(true) //
                .withName("sub") //
                .withElementSchema(subRecordSchema) //
                .build();

        final Schema recordSchema = factory //
                .newSchemaBuilder(Type.RECORD) //
                .withEntry(entryRecord) //
                .build();

        final Record firstRecord = factory.newRecordBuilder(recordSchema) //
                .withArray(entryRecord, Arrays.asList(subRecord1, subRecord2, subRecord3)) //
                .build();
        final GenericRecord genericRecord1 = converter.fromRecord(firstRecord);
        Assertions.assertNotNull(genericRecord1);
        final Object sub = genericRecord1.get("sub");
        Assertions.assertNotNull(sub);
        Assertions.assertTrue(sub instanceof Iterable, "sub is of class " + sub.getClass().getName());
    }

    @Test
    void fromSimpleRecord() {
        RecordToAvro converter = new RecordToAvro("test");
        GenericRecord record = converter.fromRecord(versatileRecord);
        assertNotNull(record);
        assertEquals("Bonjour", record.get("string1"));
        assertEquals("Olà", record.get("string2"));
        assertEquals(71, record.get("int"));
        assertEquals(true, record.get("boolean"));
        assertEquals(1971L, record.get("long"));
        assertEquals(LocalDateTime.of(2019, 04, 22, 0, 0).atZone(ZoneOffset.UTC).toInstant().toEpochMilli(),
                record.get("datetime"));
        assertEquals(20.5f, record.get("float"));
        assertEquals(20.5, record.get("double"));
    }

    @Test
    void fromComplexRecord() {
        final RecordToAvro converter = new RecordToAvro("test");
        final GenericRecord record = converter.fromRecord(complexRecord);
        assertNotNull(record);
        System.err.println(record);
        assertEquals("ComplexR", record.get("name"));
        assertNotNull(record.get("record"));
        GenericRecord subrecord = (GenericRecord) record.get("record");
        assertEquals("Bonjour", subrecord.get("string1"));
        assertEquals("Olà", subrecord.get("string2"));
        assertEquals(71, subrecord.get("int"));
        assertEquals(true, subrecord.get("boolean"));
        assertEquals(1971L, subrecord.get("long"));
        assertEquals(LocalDateTime.of(2019, 04, 22, 0, 0).atZone(ZoneOffset.UTC).toInstant().toEpochMilli(),
                subrecord.get("datetime"));
        assertEquals(20.5f, subrecord.get("float"));
        assertEquals(20.5, subrecord.get("double"));

        final long nowFromRecord = (long) record.get("now");
        assertEquals(nowFromRecord, this.now.toInstant().toEpochMilli());

        assertEquals(Arrays.asList("ary1", "ary2", "ary3"), record.get("array"));
    }

    @Test
    void fromAndToRecord() {
        RecordToAvro converter = new RecordToAvro("test");
        AvroToRecord toRecord = new AvroToRecord(this.factory);
        GenericRecord from = converter.fromRecord(versatileRecord);
        assertNotNull(from);
        Record to = toRecord.toRecord(from);
        assertNotNull(to);
        assertEquals("Bonjour", to.getString("string1"));
        assertEquals("Olà", to.getString("string2"));
        assertEquals(71, to.getInt("int"));
        assertEquals(true, to.getBoolean("boolean"));
        assertEquals(1971L, to.getLong("long"));
        assertEquals(LocalDateTime.of(2019, 04, 22, 0, 0).atZone(ZoneOffset.UTC).toInstant(),
                to.getDateTime("datetime").toInstant());
        assertEquals(20.5f, to.getFloat("float"));
        assertEquals(20.5, to.getDouble("double"));
    }

    private void prepareTestRecords() {
        // some demo records
        versatileRecord = factory.newRecordBuilder() //
                .withString("string1", "Bonjour") //
                .withString("string2", "Olà") //
                .withInt("int", 71) //
                .withBoolean("boolean", true) //
                .withLong("long", 1971L) //
                .withDateTime("datetime", LocalDateTime.of(2019, 04, 22, 0, 0).atZone(ZoneOffset.UTC)) //
                .withFloat("float", 20.5f) //
                .withDouble("double", 20.5) //
                .build();

        Entry er = factory.newEntryBuilder().withName("record").withType(Type.RECORD)
                .withElementSchema(versatileRecord.getSchema()).build();
        Entry ea = factory.newEntryBuilder().withName("array").withType(Type.ARRAY)
                .withElementSchema(factory.newSchemaBuilder(Type.ARRAY).withType(Type.STRING).build()).build();

        complexRecord = factory.newRecordBuilder() //
                .withString("name", "ComplexR") //
                .withRecord(er, versatileRecord) //
                .withDateTime("now", now) //
                .withArray(ea, Arrays.asList("ary1", "ary2", "ary3")).build();
    }

}