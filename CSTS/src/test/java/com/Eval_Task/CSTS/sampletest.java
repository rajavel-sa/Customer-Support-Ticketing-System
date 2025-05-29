package com.Eval_Task.CSTS;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SimpleTest {
    @Mock
    List<String> mockList;

    @Test
    void testMock() {
        when(mockList.size()).thenReturn(10);
        assertEquals(10, mockList.size());
    }
}