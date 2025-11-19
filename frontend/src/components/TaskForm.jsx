import React, { useState } from 'react';
import PropTypes from 'prop-types';
import '../styles/TaskForm.css';

/**
 * TaskForm Component
 * Form for creating new tasks with validation
 */
const TaskForm = ({ onSubmit, onCancel, isSubmitting }) => {
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    clientAddress: '',
    priority: 'MEDIUM',
    estimatedDuration: '',
  });

  const [errors, setErrors] = useState({});

  const validateForm = () => {
    const newErrors = {};

    // Validate title
    if (!formData.title.trim()) {
      newErrors.title = 'Title is required';
    } else if (formData.title.length < 3) {
      newErrors.title = 'Title must be at least 3 characters';
    } else if (formData.title.length > 200) {
      newErrors.title = 'Title must not exceed 200 characters';
    }

    // Validate client address
    if (!formData.clientAddress.trim()) {
      newErrors.clientAddress = 'Address is required';
    } else if (formData.clientAddress.length < 5) {
      newErrors.clientAddress = 'Address must be at least 5 characters';
    } else if (formData.clientAddress.length > 500) {
      newErrors.clientAddress = 'Address must not exceed 500 characters';
    } else if (!/\d/.test(formData.clientAddress) || !/[a-zA-Z]/.test(formData.clientAddress)) {
      newErrors.clientAddress = 'Address must contain both letters and numbers';
    }

    // Validate estimated duration (optional)
    if (formData.estimatedDuration !== '' && formData.estimatedDuration !== null && formData.estimatedDuration !== undefined) {
      const duration = Number(formData.estimatedDuration);
      if (isNaN(duration) || duration < 0) {
        newErrors.estimatedDuration = 'Duration must be a positive number';
      }
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
    // Clear error for this field when user starts typing
    if (errors[name]) {
      setErrors((prev) => ({
        ...prev,
        [name]: undefined,
      }));
    }
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    
    if (!validateForm()) {
      return;
    }

    // Prepare submission data
    const submissionData = {
      title: formData.title.trim(),
      description: formData.description.trim() || undefined,
      clientAddress: formData.clientAddress.trim(),
      priority: formData.priority,
      estimatedDuration: formData.estimatedDuration 
        ? parseInt(formData.estimatedDuration, 10) 
        : undefined,
    };

    onSubmit(submissionData);
  };

  const handleCancel = () => {
    setFormData({
      title: '',
      description: '',
      clientAddress: '',
      priority: 'MEDIUM',
      estimatedDuration: '',
    });
    setErrors({});
    if (onCancel) {
      onCancel();
    }
  };

  return (
    <form className="task-form" onSubmit={handleSubmit}>
      <h2>Create New Task</h2>

      <div className="form-group">
        <label htmlFor="title">
          Title <span className="required">*</span>
        </label>
        <input
          type="text"
          id="title"
          name="title"
          value={formData.title}
          onChange={handleChange}
          className={errors.title ? 'error' : ''}
          disabled={isSubmitting}
        />
        {errors.title && <span className="error-message">{errors.title}</span>}
      </div>

      <div className="form-group">
        <label htmlFor="description">Description</label>
        <textarea
          id="description"
          name="description"
          value={formData.description}
          onChange={handleChange}
          rows="4"
          disabled={isSubmitting}
        />
      </div>

      <div className="form-group">
        <label htmlFor="clientAddress">
          Client Address <span className="required">*</span>
        </label>
        <input
          type="text"
          id="clientAddress"
          name="clientAddress"
          value={formData.clientAddress}
          onChange={handleChange}
          className={errors.clientAddress ? 'error' : ''}
          disabled={isSubmitting}
          placeholder="e.g., 123 Main Street, City, State"
        />
        {errors.clientAddress && (
          <span className="error-message">{errors.clientAddress}</span>
        )}
      </div>

      <div className="form-row">
        <div className="form-group">
          <label htmlFor="priority">
            Priority <span className="required">*</span>
          </label>
          <select
            id="priority"
            name="priority"
            value={formData.priority}
            onChange={handleChange}
            disabled={isSubmitting}
          >
            <option value="HIGH">High</option>
            <option value="MEDIUM">Medium</option>
            <option value="LOW">Low</option>
          </select>
        </div>

        <div className="form-group">
          <label htmlFor="estimatedDuration">Estimated Duration (minutes)</label>
          <input
            type="number"
            id="estimatedDuration"
            name="estimatedDuration"
            value={formData.estimatedDuration}
            onChange={handleChange}
            min="0"
            className={errors.estimatedDuration ? 'error' : ''}
            disabled={isSubmitting}
          />
          {errors.estimatedDuration && (
            <span className="error-message">{errors.estimatedDuration}</span>
          )}
        </div>
      </div>

      <div className="form-actions">
        <button
          type="button"
          className="cancel-button"
          onClick={handleCancel}
          disabled={isSubmitting}
        >
          Cancel
        </button>
        <button type="submit" className="submit-button" disabled={isSubmitting}>
          {isSubmitting ? 'Creating...' : 'Create Task'}
        </button>
      </div>
    </form>
  );
};

TaskForm.propTypes = {
  onSubmit: PropTypes.func.isRequired,
  onCancel: PropTypes.func,
  isSubmitting: PropTypes.bool,
};

TaskForm.defaultProps = {
  isSubmitting: false,
  onCancel: null,
};

export default TaskForm;
